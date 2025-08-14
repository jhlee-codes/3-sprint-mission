package com.sprint.mission.discodeit.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.auth.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.dto.Common.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final DiscodeitUserDetailsService discodeitUserDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        try {
            log.debug("[JwtAuthenticationFilter] 요청 처리 시작: {} {}", request.getMethod(),
                request.getRequestURI());

            // 이미 인증된 경우 스킵
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                log.debug("[JwtAuthenticationFilter] 인증 컨텍스트 존재 → 토큰 검증 스킵");
                filterChain.doFilter(request, response);
                return;
            }

            String token = resolveToken(request);
            if (token != null) {
                log.debug("[JwtAuthenticationFilter] Bearer 토큰 추출 성공");

                // 토큰 유효성 검사
                if (jwtTokenProvider.validateAccessToken(token)) {
                    authenticateWithToken(token, request);
                } else {
                    log.debug("[JwtAuthenticationFilter] 토큰 유효성 검사 실패");
                    sendUnauthorized(response, "토큰이 유효하지 않습니다.");
                    return;
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.debug("[JwtAuthenticationFilter] 예외 발생: " + e.getMessage());
            SecurityContextHolder.clearContext();
            sendUnauthorized(response, "토큰 인증에 실패하였습니다.");
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void authenticateWithToken(String token, HttpServletRequest request) {
        String username = jwtTokenProvider.getUsernameFromToken(token);

        UserDetails userDetails = discodeitUserDetailsService.loadUserByUsername(username);
        log.debug("[JwtAuthenticationFilter] 사용자 정보 로드 완료: {}", username);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("[JwtAuthenticationFilter] SecurityContext 인증 설정 완료: {}", username);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiErrorResponse errorResponse = ApiErrorResponse.of(HttpStatus.UNAUTHORIZED, message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
