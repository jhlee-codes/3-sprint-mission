package com.sprint.mission.discodeit.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@Slf4j
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager redisCacheManager(
        RedisConnectionFactory cf,
        RedisCacheConfiguration redisCfg
    ) {
        return RedisCacheManager.builder(cf)
            .cacheDefaults(redisCfg)
            .build();
    }

    @Bean("caffeineCacheManager")
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        manager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(Duration.ofMinutes(10))
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

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper) {
        ObjectMapper redisObjectMapper = objectMapper.copy();
        redisObjectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            DefaultTyping.EVERYTHING,
            As.PROPERTY
        );

        return RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer(redisObjectMapper)
                )
            )
            .prefixCacheNameWith("discodeit:")
            .entryTtl(Duration.ofSeconds(600))
            .disableCachingNullValues();
    }
}
