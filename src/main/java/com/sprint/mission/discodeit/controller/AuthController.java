package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.auth.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.auth.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.Common.ApiErrorResponse;
import com.sprint.mission.discodeit.dto.JwtDto;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.dto.User.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
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

    @GetMapping("/csrf-token")
    @Override
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        String tokenValue = csrfToken.getToken();
        log.debug("CSRF 토큰 요청: {}", tokenValue);

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }

    @GetMapping("/me")
    @Override
    public ResponseEntity<UserDto> getCurrentUser(
        @AuthenticationPrincipal DiscodeitUserDetails userDetails) {

        log.debug("[AuthController] 세션 기반 사용자 정보 조회 요청(me)");

        UserDto userDto = authService.getCurrentUserInfo(userDetails);

        log.debug("[AuthController] 사용자 정보 조회 완료: " + userDto);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userDto);
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
    public ResponseEntity<?> refreshAccessToken(
        HttpServletRequest request
    ) {

        log.debug("[AuthController] RefreshToken으로 AccessToken 재발급 요청");

        String refreshToken = null;
        for (Cookie cookie : request.getCookies()) {
            if ("REFRESH_TOKEN".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null || !jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.debug("[AuthController] 유효하지 않은 RefreshToken");

            ApiErrorResponse apiErrorResponse = ApiErrorResponse.of(HttpStatus.UNAUTHORIZED,
                "Refresh Token이 유효하지 않습니다.");

            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(apiErrorResponse);
        }

        try {
            UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            DiscodeitUserDetails discodeitUserDetails = discodeitUserDetailsService.loadUserByUserId(
                userId);
            String newAccessToken = jwtTokenProvider.generateAccessToken(discodeitUserDetails);
            UserDto userDto = discodeitUserDetails.getUserDto();
            JwtDto jwtDto = new JwtDto(userDto, newAccessToken);

            log.debug("[AuthController] Refresh 토큰으로 AccessToken 재발급 완료");
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(jwtDto);
        } catch (Exception e) {
            log.error("[AuthController] 토큰 재발급 중 예외", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 재발급 중 오류가 발생했습니다."));
        }
    }
}
