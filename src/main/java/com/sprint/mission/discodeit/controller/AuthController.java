package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.User.UserLoginRequestDTO;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
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
            path = "/login",
            method = RequestMethod.POST
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
