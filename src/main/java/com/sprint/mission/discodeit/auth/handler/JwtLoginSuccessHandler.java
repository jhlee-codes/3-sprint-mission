package com.sprint.mission.discodeit.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.auth.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.auth.jwt.store.JwtRegistry;
import com.sprint.mission.discodeit.dto.Common.ApiErrorResponse;
import com.sprint.mission.discodeit.dto.JwtDto;
import com.sprint.mission.discodeit.dto.JwtInformation;
import com.sprint.mission.discodeit.dto.User.UserDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {

        log.debug("[JwtLoginSuccessHandler] 로그인 성공 처리 시작");

        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        if (authentication.getPrincipal() instanceof DiscodeitUserDetails discodeitUserDetails) {
            try {
                log.debug("[JwtLoginSuccessHandler] 새 토큰 발급 시작: username={}",
                    discodeitUserDetails.getUsername());

                UserDto userDto = discodeitUserDetails.getUserDto();

                log.debug("[JwtLoginSuccessHandler] 동일 계정의 기존 로그인 모두 무효화");
                jwtRegistry.invalidateJwtInformationByUserId(userDto.id());

                String accessToken = jwtTokenProvider.generateAccessToken(discodeitUserDetails);
                String refreshToken = jwtTokenProvider.generateRefreshToken(discodeitUserDetails);
                JwtDto jwtDto = new JwtDto(userDto, accessToken);
                
                log.debug("[JwtLoginSuccessHandler] jwtRegistry에 JwtInformation 등록 시작");
                jwtRegistry.registerJwtInformation(
                    new JwtInformation(userDto, accessToken, refreshToken));

                log.debug("[JwtLoginSuccessHandler] 리프레시 쿠키 설정 시작");
                jwtTokenProvider.addRefreshCookie(response, refreshToken);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(objectMapper.writeValueAsString(jwtDto));

                log.debug("[JwtLoginSuccessHandler] 로그인 성공 응답 완료: {}", userDto.username());
            } catch (Exception e) {
                log.error("[JwtLoginSuccessHandler] 토큰 생성/응답 처리 중 예외 발생: " + e.getMessage());

                String errorMsg = "토큰 생성/응답 처리 중 예외가 발생하였습니다.";
                ApiErrorResponse errorResponse = ApiErrorResponse.of(
                    HttpStatus.INTERNAL_SERVER_ERROR, errorMsg);

                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            }
        } else {
            String errorMsg = "인증 정보를 처리할 수 없습니다.";
            ApiErrorResponse errorResponse = ApiErrorResponse.of(HttpStatus.UNAUTHORIZED, errorMsg);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));

            log.debug(
                "[JwtLoginSuccessHandler] 예상치 못한 Principal 타입: " + authentication.getPrincipal()
                    .getClass());
        }
    }
}
