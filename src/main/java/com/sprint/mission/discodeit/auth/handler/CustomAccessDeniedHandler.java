package com.sprint.mission.discodeit.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        AccessDeniedException e) throws IOException, ServletException {

        log.debug("[CustomAccessDeniedHandler] 접근 거부 응답 요청");

        try {
            ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                "ACCESS_DENIED",
                "해당 리소스에 접근할 권한이 없습니다.",
                Map.of(),
                e.getClass().getSimpleName(),
                HttpStatus.FORBIDDEN.value()
            );

            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String responseBody = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(responseBody);

            log.debug("[CustomAccessDeniedHandler] 접근 거부 응답 완료");
        } catch (Exception ex) {
            log.debug("AccessDeniedHandler 처리 중 예외 발생", ex);
        }
    }
}
