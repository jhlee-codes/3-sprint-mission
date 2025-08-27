package com.sprint.mission.discodeit.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        manager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .recordStats()
            .removalListener((key, value, cause) -> {
                switch (cause) {
                    case SIZE:
                        log.debug("캐시 크기 초과로 인한 엔트리 제거 - key: {}", key);
                        break;
                    case EXPIRED:
                        log.debug("캐시 만료로 인한 엔트리 제거 - key: {}", key);
                        break;
                    case EXPLICIT:
                        log.info("캐시 수동 삭제로 인한 엔트리 제거 - key: {}", key);
                        break;
                    case REPLACED:
                        log.debug("캐시 새 값으로 교체로 인한 엔트리 제거 - key: {}", key);
                        break;
                    default:
                        log.debug("캐시 엔트리 제거 - key: {}, cause: {}", key, cause);
                }

            })
        );

        manager.setCacheNames(List.of("user:channels", "user:notifications", "users:list"));
        return manager;
    }
}
