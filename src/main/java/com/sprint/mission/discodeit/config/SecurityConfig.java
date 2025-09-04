package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.auth.handler.CustomAccessDeniedHandler;
import com.sprint.mission.discodeit.auth.handler.JwtLoginSuccessHandler;
import com.sprint.mission.discodeit.auth.handler.JwtLogoutHandler;
import com.sprint.mission.discodeit.auth.handler.LoginFailureHandler;
import com.sprint.mission.discodeit.auth.handler.SpaCsrfTokenRequestHandler;
import com.sprint.mission.discodeit.auth.jwt.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.auth.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.auth.jwt.store.InMemoryJwtRegistry;
import com.sprint.mission.discodeit.auth.jwt.store.JwtRegistry;
import com.sprint.mission.discodeit.entity.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
    public RoleHierarchy roleHierarchy() {
        // 관리자 > 채널 매니저 > 일반 사용자
        RoleHierarchyImpl roleHierarchy = RoleHierarchyImpl.fromHierarchy(
            "ROLE_ADMIN > ROLE_CHANNEL_MANAGER > ROLE_USER");
        log.debug("[SecurityConfig] RoleHierarchy 설정 완료");
        return roleHierarchy;
    }

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
        RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        log.debug("[SecurityConfig] MethodSecurityExpressionHandler 설정 완료");
        return handler;
    }

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        LoginFailureHandler loginFailureHandler,
        CustomAccessDeniedHandler accessDeniedHandler,
        JwtAuthenticationFilter jwtAuthenticationFilter,
        JwtLoginSuccessHandler jwtLoginSuccessHandler,
        JwtLogoutHandler jwtLogoutHandler) throws Exception {

        log.debug("[SecurityConfig] FilterChain 구성 시작");

        http
            // CSRF 설정
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
            )

            // 요청 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/index.html", "/favicon.ico", "/assets/**",
                    "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**",
                    "/error", "/error/**",
                    "/ws/**"
                ).permitAll()

                .requestMatchers(
                    AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/auth/csrf-token"),
                    AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/users"),
                    AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/login"),
                    AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/logout"),
                    AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/refresh")
                ).permitAll()

                // 퍼블릭 채널 관리: CHANNEL_MANAGER
                .requestMatchers(HttpMethod.POST, "/api/channels/public")
                .hasRole(Role.CHANNEL_MANAGER.name())
                .requestMatchers(HttpMethod.PATCH, "/api/channels/*")
                .hasRole(Role.CHANNEL_MANAGER.name())
                .requestMatchers(HttpMethod.DELETE, "/api/channels/*")
                .hasRole(Role.CHANNEL_MANAGER.name())

                // 사용자 권한 변경: ADMIN
                .requestMatchers(HttpMethod.PUT, "/api/auth/role").hasRole(Role.ADMIN.name())
                .anyRequest().authenticated()
            )

            // 세션 관리 설정
            .sessionManagement(management -> management
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 폼 기반 로그인 설정
            .formLogin(login -> login
                .loginProcessingUrl("/api/auth/login")
                .successHandler(jwtLoginSuccessHandler)
                .failureHandler(loginFailureHandler)
            )

            // 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .addLogoutHandler(jwtLogoutHandler)
                .logoutSuccessHandler(
                    new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
            )

            // 예외 처리 설정
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new Http403ForbiddenEntryPoint())
                .accessDeniedHandler(accessDeniedHandler)
            )

            // JWT 인증 필터 UsernamePasswordAuthenticationFilter 이전에 배치
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.debug("[SecurityConfig] FilterChain 구성 완료");
        return http.build();
    }

    @Bean
    public SessionRegistry sessionRegistry() {

        SessionRegistryImpl sessionRegistry = new SessionRegistryImpl() {

            // 새 세션 등록 시 로깅
            @Override
            public void registerNewSession(String sessionId, Object principal) {
                log.debug("[SessionRegistry] 새 세션 등록 - 사용자: {}, 세션ID: {} ", principal, sessionId);
                super.registerNewSession(sessionId, principal);
            }

            // 세션 제거 시 로깅
            @Override
            public void removeSessionInformation(String sessionId) {
                log.debug("[SessionRegistry] 세션 제거 - 세션ID: {}", sessionId);
                super.removeSessionInformation(sessionId);
            }
        };

        return sessionRegistry;
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public JwtRegistry jwtRegistry(JwtTokenProvider jwtTokenProvider) {
        return new InMemoryJwtRegistry(1, jwtTokenProvider);
    }
}
