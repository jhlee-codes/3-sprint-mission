package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.handler.LoginFailureHandler;
import com.sprint.mission.discodeit.handler.LoginSuccessHandler;
import com.sprint.mission.discodeit.service.DiscodeitUserDetailsService;
import java.util.List;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${remember-me.key}")
    private String rememberMeKey;

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

//    @Bean
//    JdbcTokenRepositoryImpl tokenRepository(DataSource dataSource) {
//
//        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
//        tokenRepository.setDataSource(dataSource);
//
//        log.debug("[SecurityConfig] JdbcTokenRepository 생성 완료");
//        return tokenRepository;
//    }

    @Bean
    public TokenBasedRememberMeServices rememberMeServices(
        DiscodeitUserDetailsService userDetailsService) {

        TokenBasedRememberMeServices rememberMeServices =
            new TokenBasedRememberMeServices(
                rememberMeKey,
                userDetailsService
            );

        // 유지 기간: 7일
        rememberMeServices.setTokenValiditySeconds(7 * 24 * 60 * 60);
        rememberMeServices.setCookieName("remember-me");
        rememberMeServices.setParameter("remember-me");

        log.debug("[SecurityConfig] Remember-Me 설정 완료");

        return rememberMeServices;
    }

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        LoginSuccessHandler loginSuccessHandler,
        LoginFailureHandler loginFailureHandler,
        SessionRegistry sessionRegistry,
        TokenBasedRememberMeServices rememberMeService
    ) throws Exception {

        log.debug("[SecurityConfig] FilterChain 구성 시작");

        http
            // CSRF 설정
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )

            // 요청 권한 설정
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/", "/index.html", "/favicon.ico", "/assets/**",
                    "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**"
                ).permitAll()

                .requestMatchers("/api/auth/csrf-token").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/logout").permitAll()

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
                .sessionConcurrency(concurrency -> concurrency
                    .maximumSessions(1)
                    .sessionRegistry(sessionRegistry)
                )
            )

            // 폼 기반 로그인 설정
            .formLogin(login -> login
                .loginProcessingUrl("/api/auth/login")
                .successHandler(loginSuccessHandler)
                .failureHandler(loginFailureHandler)
            )

            // 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(
                    new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
            )

            // Remember-Me 설정
            .rememberMe(remember -> remember
                .rememberMeServices(rememberMeService)
                .key(rememberMeKey)
            )

            // 예외 처리 설정
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler(new AccessDeniedHandlerImpl())
            )
        ;

        log.debug("[SecurityConfig] FilterChain 구성 완료");
        return http.build();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
