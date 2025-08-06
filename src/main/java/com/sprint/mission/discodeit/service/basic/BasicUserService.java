package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.annotation.Logging;
import com.sprint.mission.discodeit.dto.BinaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.User.UserCreateRequest;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.dto.User.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.User.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.User.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@Logging
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SessionRegistry sessionRegistry;

    /**
     * 주어진 생성 요청 DTO(유저, 프로필사진)를 기반으로 유저 생성
     *
     * @param userCreateRequest    유저 생성 요청 DTO
     * @param profileCreateRequest 프로필사진 생성 요청 DTO
     * @return 생성된 유저
     * @throws UserAlreadyExistsException 유저명/이메일이 중복된 경우
     */
    @Override
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
            binaryContentStorage.put(binaryContent.getId(), profileCreateRequest.bytes());
        }

        String encodedPassword = passwordEncoder.encode(userCreateRequest.password());

        User user = User.builder()
            .username(username)
            .email(email)
            .password(encodedPassword)
            .profile(binaryContent)
            .build();

        userRepository.save(user);

        UserDto userDto = userMapper.toDto(user);
        return setUserDtoWithOnline(userDto);
    }

    /**
     * 레포지토리로부터 읽어온 유저 데이터 전체 조회
     *
     * @return 조회된 유저 데이터
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {

        List<User> users = userRepository.findAll();

        return users.stream()
            .map(userMapper::toDto)
            .map(this::setUserDtoWithOnline)
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

        UserDto userDto = userMapper.toDto(user);
        return setUserDtoWithOnline(userDto);
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
            binaryContentStorage.put(binaryContent.getId(), profileCreateRequest.bytes());
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

        UserDto userDto = userMapper.toDto(user);
        return setUserDtoWithOnline(userDto);
    }

    /**
     * 주어진 id에 해당하는 유저 삭제
     *
     * @param userId 삭제할 대상 유저 id
     * @throws UserNotFoundException 유저가 존재하지 않는 경우
     */
    @Override
    @Transactional
    public void delete(UUID userId) {
        log.info("유저 삭제 요청: ID = {}", userId);

        if (!userRepository.existsById(userId)) {
            log.warn("유저 삭제 실패: 존재하지 않는 유저: ID = {}", userId);
            throw UserNotFoundException.byId(userId);
        }

        userRepository.deleteById(userId);
        log.info("유저 삭제 완료: ID = {}", userId);
    }

    @Override
    @Transactional
    public UserDto updateUserRole(UUID userId, Role newRole) {

        log.info("유저 권한 변경 요청: ID = {}, Role = {}", userId, newRole);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));

        user.updateRole(newRole);

        User updateUser = userRepository.save(user);

        // 사용자의 모든 활성 세션 무효화
        invalidateUserSession(updateUser.getUsername());

        log.info("유저 권한 변경 완료: ID = {}, Role = {}", userId, newRole);

        UserDto userDto = userMapper.toDto(updateUser);
        return setUserDtoWithOnline(userDto);
    }

    private void invalidateUserSession(String username) {
        try {
            log.debug("세션 무효화 요청: {}", username);

            List<Object> allPrincipals = sessionRegistry.getAllPrincipals();

            for (Object principal : allPrincipals) {

                UserDetails user = (UserDetails) principal;
                String principalName = user.getUsername();

                if (username.equals(principalName)) {

                    List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal,
                        false);
                    log.debug("대상 사용자의 활성 세션 수: {}", sessions.size());

                    for (SessionInformation session : sessions) {
                        log.debug("세션 무효화 시작 - 세션 ID: {}", session.getSessionId());
                        session.expireNow();
                        log.debug("세션 무효화 완료 - 만료됨: {}", session.isExpired());
                    }

                    log.debug("대상 사용자 '{}'의 모든 세션 무효화 완료", username);
                    break;
                }
            }
            log.debug("세션 무효화 완료");
        } catch (Exception e) {
            log.debug("세션 무효화 중 오류 발생: {}", e.getMessage());
        }
    }

    private UserDto setUserDtoWithOnline(UserDto userDto) {
        return userDto.toBuilder()
            .online(authService.isUserOnline(userDto.username()))
            .build();
    }
}
