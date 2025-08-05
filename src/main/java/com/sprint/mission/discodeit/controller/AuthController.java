package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.DiscodeitUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController implements AuthApi {

    private final AuthService authService;

    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        String tokenValue = csrfToken.getToken();
        log.debug("CSRF 토큰 요청: {}", tokenValue);

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(
        @AuthenticationPrincipal DiscodeitUserDetails userDetails) {

        log.debug("[AuthController] 세션 기반 사용자 정보 조회 요청(me)");

        if (userDetails == null) {
            log.debug("[AuthController] 인증되지 않은 사용자");
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }

        UserDto userDto = authService.getCurrentUserInfo(userDetails);
        
        log.debug("[AuthController] 사용자 정보 조회 완료: " + userDto);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userDto);
    }
}
