package com.sprint.mission.discodeit.auth;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.redis.RedisLockProvider;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {

    private static final String ADMIN_ACCOUNT_INITIALIZER_LOCK = "ADMIN_ACCOUNT_INITIALIZER";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisLockProvider redisLockProvider;

    @Value("${admin.email}")
    private String DEFAULT_ADMIN_EMAIL;
    @Value("${admin.username}")
    private String DEFAULT_ADMIN_USERNAME;
    @Value("${admin.password}")
    private String DEFAULT_ADMIN_PASSWORD;

    @Override
    @Transactional
    public void run(String... args) {
        boolean lockAcquired = false;
        try {
            lockAcquired = redisLockProvider.acquireLock(ADMIN_ACCOUNT_INITIALIZER_LOCK);
            if (lockAcquired) {
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
            } else {
                log.info("다른 인스턴스에서 Admin 계정 생성 중");
            }
        } finally {
            if (lockAcquired) {
                redisLockProvider.releaseLock(ADMIN_ACCOUNT_INITIALIZER_LOCK);
            }
        }
    }
}