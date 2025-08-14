package com.sprint.mission.discodeit.auth.handler;

import com.sprint.mission.discodeit.auth.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) {
        log.debug("[JwtLogoutHandler] 로그아웃 처리 시작");

        // 쿠키에 저장된 리프레시 토큰 삭제
        jwtTokenProvider.expireRefreshCookie(response);

        log.debug("[JwtLogoutHandler] 로그아웃 처리 완료");
    }
}
