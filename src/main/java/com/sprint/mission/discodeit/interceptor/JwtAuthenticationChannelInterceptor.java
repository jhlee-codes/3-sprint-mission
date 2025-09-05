package com.sprint.mission.discodeit.interceptor;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.auth.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.auth.jwt.store.JwtRegistry;
import com.sprint.mission.discodeit.exception.Auth.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final DiscodeitUserDetailsService discodeitUserDetailsService;
    private final JwtRegistry jwtRegistry;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
            StompHeaderAccessor.class);

        // CONNECT 메시지일 때만 인증 로직 수행
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.debug("[JwtAuthenticationChannelInterceptor] 엑세스 토큰 검증 시작");
            String token = resolveToken(accessor);

            if (token != null) {
                log.debug("[JwtAuthenticationChannelInterceptor] Bearer 토큰 추출 성공");

                // 토큰 유효성 검사
                if (jwtTokenProvider.validateAccessToken(token)
                    && jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
                    authenticateWithToken(token, accessor);
                    log.debug("[JwtAuthenticationChannelInterceptor] 엑세스 토큰 검증 완료");
                } else {
                    log.error("[JwtAuthenticationChannelInterceptor] 토큰 유효성 검사 실패");
                    throw new InvalidTokenException("유효하지 않은 토큰입니다.");
                }
            }
        }
        return message;
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        // STOMP CONNECT 프레임의 네이티브 헤더에서 Authorization 값을 가져옴
        String bearerToken = accessor.getFirstNativeHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void authenticateWithToken(String token, StompHeaderAccessor accessor) {
        String username = jwtTokenProvider.getUsernameFromToken(token);

        UserDetails userDetails = discodeitUserDetailsService.loadUserByUsername(username);
        log.debug("[JwtAuthenticationChannelInterceptor] 사용자 정보 로드 완료: {}", username);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            );

        // STOMP 세션에 인증 정보 설정
        accessor.setUser(authentication);
        log.debug("[JwtAuthenticationChannelInterceptor] STOMP 세션 인증 설정 완료: {}",
            username);
    }
}