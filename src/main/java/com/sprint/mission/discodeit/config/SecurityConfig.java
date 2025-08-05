package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.handler.LoginFailureHandler;
import com.sprint.mission.discodeit.handler.LoginSuccessHandler;
import java.util.List;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner debugFilterChan(SecurityFilterChain filterChain) {
        return args -> {
            int filterSize = filterChain.getFilters().size();

            List<String> filterNames = IntStream.range(0, filterSize)
                .mapToObj(idx -> String.format("\t[%s/%s] %s", idx + 1, filterSize,
                    filterChain.getFilters().get(idx).getClass()))
                .toList();

            log.debug("현재 적용된 필터 체인 목록:");
            filterNames.forEach(log::debug);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
        LoginSuccessHandler loginSuccessHandler,
        LoginFailureHandler loginFailureHandler) throws Exception {

        log.debug("[SecurityConfig] FilterChain 구성 시작");

        http
            // CSRF 설정
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )

            // 폼 기반 로그인 설정
            .formLogin(login -> login
                .loginProcessingUrl("/api/auth/login")
                .successHandler(loginSuccessHandler)
                .failureHandler(loginFailureHandler)
            )

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(
                    new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
            )
        ;

        log.debug("[SecurityConfig] FilterChain 구성 완료");

        return http.build();
    }
}
