package com.sprint.mission.discodeit.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {

        log.debug("[LoginSuccessHandler] 로그인 성공 처리 시작");

        if (authentication.getPrincipal() instanceof DiscodeitUserDetails discodeitUserDetails) {
            UserDto userDto = discodeitUserDetails.getUserDto();

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);

            String responseBody = objectMapper.writeValueAsString(userDto);
            response.getWriter().write(responseBody);

            log.debug("[LoginSuccessHandler] 로그인 성공 응답 완료: " + userDto.username());
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"인증 정보를 처리할 수 없습니다.\"}");

            log.debug("[LoginSuccessHandler] 예상치 못한 Principal 타입: " + authentication.getPrincipal()
                .getClass());
        }
    }
}
