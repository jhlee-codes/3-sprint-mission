package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.User.UserLoginRequestDTO;
import com.sprint.mission.discodeit.dto.UserStatus.UserStatusUpdateRequestDTO;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.Instant;
import java.util.UUID;
import org.springframework.web.bind.annotation.ResponseBody;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@ResponseBody
@Controller
public class AuthController {

    public final AuthService authService;

    /**
     * 사용자 로그인 인증
     *
     * @param userLoginRequestDTO 유저 로그인 요청 DTO
     * @return 로그인된 User(HTTP 200 OK)
     */
    @RequestMapping(
            path = "/login"
    )
    public ResponseEntity<User> login(
            @RequestBody UserLoginRequestDTO userLoginRequestDTO
    ) {
        // 로그인
        User authUser = authService.login(userLoginRequestDTO);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authUser);
    }
}
