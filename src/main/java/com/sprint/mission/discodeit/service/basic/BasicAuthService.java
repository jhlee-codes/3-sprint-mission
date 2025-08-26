package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.auth.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.auth.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.auth.jwt.store.JwtRegistry;
import com.sprint.mission.discodeit.dto.JwtDto;
import com.sprint.mission.discodeit.dto.JwtInformation;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.Auth.InvalidTokenException;
import com.sprint.mission.discodeit.exception.User.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final DiscodeitUserDetailsService discodeitUserDetailsService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUserInfo(DiscodeitUserDetails userDetails) {

        log.debug("[AuthService] 사용자 정보 조회 요청");

        UUID userId = userDetails.getId();

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        log.debug("[AuthService] 조회된 사용자 정보: {}", user);

        return userMapper.toDto(user);
    }

    @Override
    public JwtDto refreshToken(String refreshToken, HttpServletResponse response) {

        log.debug("[AuthService] RefreshToken으로 AccessToken 재발급 시작");

        if (!jwtTokenProvider.validateRefreshToken(refreshToken)
            || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
            log.debug("[AuthService] 유효하지 않은 RefreshToken");
            throw new InvalidTokenException(refreshToken);
        }

        try {
            UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            DiscodeitUserDetails discodeitUserDetails = discodeitUserDetailsService.loadUserByUserId(
                userId);
            UserDto userDto = discodeitUserDetails.getUserDto();

            String newAccessToken = jwtTokenProvider.generateAccessToken(discodeitUserDetails);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(discodeitUserDetails);

            log.debug("[AuthService] JwtInformation 회전 시작");
            boolean rotatedSuccess = jwtRegistry.rotateJwtInformation(refreshToken,
                new JwtInformation(userDto, newAccessToken, newRefreshToken));

            if (!rotatedSuccess) {
                log.warn("[AuthService] Refresh 회전 실패 - userId={}", userId);
                jwtTokenProvider.expireRefreshCookie(response);
                throw new InvalidTokenException("refresh-rotation-failed");
            }

            // RefreshToken Rotation
            jwtTokenProvider.addRefreshCookie(response, newRefreshToken);
            log.debug("[AuthService] Refresh 토큰으로 AccessToken 재발급 완료");
            return new JwtDto(userDto, newAccessToken);
        } catch (Exception e) {
            log.error("[AuthService] 토큰 재발급 중 예외", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public UserDto updateUserRole(UUID userId, Role newRole) {

        log.info("유저 권한 변경 요청: ID = {}, Role = {}", userId, newRole);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));

        user.updateRole(newRole);
        User updateUser = userRepository.save(user);

        log.info("사용자의 JwtInformation 정보 무효화 시작");
        jwtRegistry.invalidateJwtInformationByUserId(updateUser.getId());

        RoleUpdatedEvent event = new RoleUpdatedEvent(userId, newRole);
        eventPublisher.publishEvent(event);

        log.info("유저 권한 변경 완료: ID = {}, Role = {}", userId, newRole);

        return userMapper.toDto(updateUser);
    }
}
