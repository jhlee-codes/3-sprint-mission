package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.annotation.Logging;
import com.sprint.mission.discodeit.dto.BinaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.User.UserCreateRequest;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.dto.User.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.User.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.User.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.SseService;
import com.sprint.mission.discodeit.service.UserService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@Logging
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final SseService sseService;

    /**
     * 주어진 생성 요청 DTO(유저, 프로필사진)를 기반으로 유저 생성
     *
     * @param userCreateRequest    유저 생성 요청 DTO
     * @param profileCreateRequest 프로필사진 생성 요청 DTO
     * @return 생성된 유저
     * @throws UserAlreadyExistsException 유저명/이메일이 중복된 경우
     */
    @Override
    @CacheEvict(value = "users:list", allEntries = true)
    @Transactional
    public UserDto create(UserCreateRequest userCreateRequest,
        BinaryContentCreateRequest profileCreateRequest) {
        log.info("유저 생성 요청: 유저명 = {}, 이메일 = {}", userCreateRequest.username(),
            userCreateRequest.email());

        String username = userCreateRequest.username();
        String email = userCreateRequest.email();

        if (userRepository.existsByUsername(username)) {
            log.warn("유저 생성 실패: 이미 존재하는 유저명");
            throw UserAlreadyExistsException.byUserName(username);
        }
        if (userRepository.existsByEmail(email)) {
            log.warn("유저 생성 실패: 이미 존재하는 이메일");
            throw UserAlreadyExistsException.byEmail(email);
        }

        boolean isProfileCreated = profileCreateRequest != null;
        BinaryContent binaryContent = null;

        if (isProfileCreated) {
            binaryContent = BinaryContent.builder()
                .fileName(profileCreateRequest.fileName())
                .contentType(profileCreateRequest.contentType())
                .size(((long) profileCreateRequest.bytes().length))
                .build();

            binaryContentRepository.save(binaryContent);
            BinaryContentCreatedEvent event = new BinaryContentCreatedEvent(binaryContent.getId(),
                profileCreateRequest.bytes());
            eventPublisher.publishEvent(event);
        }

        String encodedPassword = passwordEncoder.encode(userCreateRequest.password());

        User user = User.builder()
            .username(username)
            .email(email)
            .password(encodedPassword)
            .profile(binaryContent)
            .build();

        userRepository.save(user);
        UserDto savedUserDto = userMapper.toDto(user);

        // SSE Send
        sseService.broadcast("users.created",
            savedUserDto);

        return savedUserDto;
    }

    /**
     * 레포지토리로부터 읽어온 유저 데이터 전체 조회
     *
     * @return 조회된 유저 데이터
     */
    @Override
    @Cacheable(value = "users:list", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {

        List<User> users = userRepository.findAll();

        return users.stream()
            .map(userMapper::toDto)
            .toList();
    }

    /**
     * 주어진 id에 해당하는 유저 조회
     *
     * @param userId 조회할 유저의 ID
     * @return 조회된 유저 DTO
     * @throws UserNotFoundException 유저가 존재하지 않는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public UserDto find(UUID userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));

        return userMapper.toDto(user);
    }

    /**
     * 주어진 ID에 해당하는 유저를 수정 요청 DTO(유저, 프로필사진) 값으로 수정
     *
     * @param userId               수정할 유저 ID
     * @param updateRequest        유저 수정 요청 DTO
     * @param profileCreateRequest 프로필사진 수정 요청 DTO
     * @return 수정된 유저
     * @throws UserNotFoundException      유저가 존재하지 않는 경우
     * @throws UserAlreadyExistsException 신규 유저명/이메일이 중복된 경우
     */
    @Override
    @Transactional
    @CacheEvict(value = "users:list", allEntries = true)
    public UserDto update(UUID userId, UserUpdateRequest updateRequest,
        BinaryContentCreateRequest profileCreateRequest) {
        log.info("유저 수정 요청: 유저명 = {}, 이메일 = {}", updateRequest.newUsername(),
            updateRequest.newEmail());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));

        String newUsername = updateRequest.newUsername();
        String newEmail = updateRequest.newEmail();

        if (userRepository.existsByUsername(newUsername)) {
            log.warn("유저 수정 실패: 이미 존재하는 유저명");
            throw UserAlreadyExistsException.byUserName(newUsername);
        }
        if (userRepository.existsByEmail(newEmail)) {
            log.warn("유저 수정 실패: 이미 존재하는 이메일");
            throw UserAlreadyExistsException.byEmail(newEmail);
        }

        boolean isProfileCreated = profileCreateRequest != null;
        BinaryContent binaryContent = null;

        if (isProfileCreated) {
            binaryContent = BinaryContent.builder()
                .fileName(profileCreateRequest.fileName())
                .contentType(profileCreateRequest.contentType())
                .size(((long) profileCreateRequest.bytes().length))
                .build();

            binaryContentRepository.save(binaryContent);
            BinaryContentCreatedEvent event = new BinaryContentCreatedEvent(binaryContent.getId(),
                profileCreateRequest.bytes());
            eventPublisher.publishEvent(event);
        }

        String newPassword = updateRequest.newPassword();
        String encodedPassword =
            StringUtils.hasText(newPassword) ? passwordEncoder.encode(newPassword)
                : null;

        user.update(
            newUsername,
            newEmail,
            encodedPassword,
            binaryContent
        );

        UserDto savedUserDto = userMapper.toDto(user);

        // SSE Send
        sseService.broadcast("users.updated",
            savedUserDto);

        return savedUserDto;
    }

    /**
     * 주어진 id에 해당하는 유저 삭제
     *
     * @param userId 삭제할 대상 유저 id
     * @throws UserNotFoundException 유저가 존재하지 않는 경우
     */
    @Override
    @Transactional
    @CacheEvict(value = "users:list", allEntries = true)
    public void delete(UUID userId) {

        log.info("유저 삭제 요청: ID = {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));
        UserDto savedUserDto = userMapper.toDto(user);

        userRepository.deleteById(userId);
        log.info("유저 삭제 완료: ID = {}", userId);

        // SSE Send
        sseService.broadcast("users.deleted",
            savedUserDto);
    }
}
