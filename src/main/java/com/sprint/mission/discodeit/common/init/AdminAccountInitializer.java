package com.sprint.mission.discodeit.common.init;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String DEFAULT_ADMIN_EMAIL;
    @Value("${admin.username}")
    private String DEFAULT_ADMIN_USERNAME;
    @Value("${admin.password}")
    private String DEFAULT_ADMIN_PASSWORD;

    @Transactional
    public void run(String... args) {

        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        log.info("기본 Admin 계정 생성 요청");

        User user = User.builder()
            .email(DEFAULT_ADMIN_EMAIL)
            .username(DEFAULT_ADMIN_USERNAME)
            .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
            .build();

        user.updateRole(Role.ADMIN);
        userRepository.save(user);

        log.info("기본 Admin 계정 생성 완료");
    }
}