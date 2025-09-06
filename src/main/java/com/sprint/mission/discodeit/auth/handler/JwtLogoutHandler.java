package com.sprint.mission.discodeit.auth.handler;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.auth.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.auth.jwt.store.JwtRegistry;
import com.sprint.mission.discodeit.event.UserLogInOutEvent;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) {

        log.debug("[JwtLogoutHandler] 로그아웃 처리 시작");

        UUID userId = null;
        if (request.getCookies() != null) {
            Cookie refreshTokenCookie = Arrays.stream(request.getCookies())
                .filter(
                    cookie -> cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
                .findFirst()
                .orElse(null);

            if (refreshTokenCookie != null) {
                String refreshToken = refreshTokenCookie.getValue();
                userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
                if (userId != null) {
                    jwtRegistry.invalidateJwtInformationByUserId(userId);
                }
            }
        }

        if (userId == null && authentication != null
            && authentication.getPrincipal() instanceof DiscodeitUserDetails userDetails) {
            userId = userDetails.getUserDto().id();
            jwtRegistry.invalidateJwtInformationByUserId(userId);
        }

        // 클라이언트의 리프레시 토큰 쿠키 즉시 만료
        Cookie refreshTokenExpirationCookie = jwtTokenProvider.generateRefreshTokenExpirationCookie();
        response.addCookie(refreshTokenExpirationCookie);

        log.debug("[JwtLogoutHandler] 로그인/로그아웃 이벤트 발행");
        eventPublisher.publishEvent(new UserLogInOutEvent(userId, false));

        log.debug("[JwtLogoutHandler] 로그아웃 처리 완료");
    }
}
