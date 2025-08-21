package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.auth.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.auth.jwt.store.JwtRegistry;
import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.JwtDto;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.dto.User.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final DiscodeitUserDetailsService discodeitUserDetailsService;
    private final JwtRegistry jwtRegistry;

    @GetMapping("/csrf-token")
    @Override
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        String tokenValue = csrfToken.getToken();
        log.debug("CSRF 토큰 요청: {}", tokenValue);

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }

    @PutMapping("/role")
    @Override
    public ResponseEntity<UserDto> updateUserRole(
        @RequestBody UserRoleUpdateRequest roleUpdateRequest
    ) {
        log.debug("[AuthController] 사용자 권한 변경 요청");

        UserDto userDto = userService.updateUserRole(
            roleUpdateRequest.userId(),
            roleUpdateRequest.newRole()
        );

        log.debug("[AuthController] 사용자 권한 변경 성공: {}", userDto);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userDto);
    }

    @PostMapping("/refresh")
    @Override
    public ResponseEntity<?> refreshAccessToken(
        @CookieValue(
            name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
            required = false
        ) String refreshToken,
        HttpServletResponse response
    ) {

        log.debug("[AuthController] RefreshToken으로 AccessToken 재발급 요청");

        JwtDto jwtDto = authService.refreshToken(refreshToken, response);

        log.debug("[AuthController] Refresh 토큰으로 AccessToken 재발급 완료");
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(jwtDto);
    }
}
