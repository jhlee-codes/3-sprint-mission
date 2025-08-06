package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SessionRegistry sessionRegistry;

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUserInfo(DiscodeitUserDetails userDetails) {

        log.debug("[AuthService] 사용자 정보 조회 요청");

        UUID userId = userDetails.getId();

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        log.debug("[AuthService] 조회된 사용자 정보: {}", user);

        return userMapper.toDto(user)
            .toBuilder()
            .online(isUserOnline(user.getUsername()))
            .build();
    }

    @Override
    public boolean isUserOnline(String username) {
        return sessionRegistry.getAllPrincipals().stream()
            .filter(DiscodeitUserDetails.class::isInstance)
            .map(DiscodeitUserDetails.class::cast)
            .filter(user -> user.getUsername().equals(username))
            .anyMatch(user -> !sessionRegistry.getAllSessions(user, false).isEmpty());
    }
}
