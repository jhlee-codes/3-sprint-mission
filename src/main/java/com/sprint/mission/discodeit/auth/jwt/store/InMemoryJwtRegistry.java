package com.sprint.mission.discodeit.auth.jwt.store;

import com.sprint.mission.discodeit.auth.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.dto.JwtInformation;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InMemoryJwtRegistry implements JwtRegistry {

    private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
    private final Set<String> accessTokenIndexes = ConcurrentHashMap.newKeySet();
    private final Set<String> refreshTokenIndexes = ConcurrentHashMap.newKeySet();
    private final int maxActiveJwtCount;

    public InMemoryJwtRegistry(@Value("${jwt.max-active-count:1}") int maxActiveJwtCount) {
        this.maxActiveJwtCount = maxActiveJwtCount;
    }

    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {

        UUID userId = jwtInformation.userDto().id();
        log.debug("[InMemoryJwtRegistry] JwtInformation 등록 시작 - userId = {}", userId);

        origin.compute(userId, (id, q) -> {
            Queue<JwtInformation> queue = (q == null) ? new ConcurrentLinkedQueue<>() : q;

            while (queue.size() >= maxActiveJwtCount) {
                JwtInformation removed = queue.poll();
                if (removed != null) {
                    removeTokenIndex(
                        removed.accessToken(),
                        removed.refreshToken()
                    );
                }
            }

            queue.offer(jwtInformation);
            addTokenIndex(
                jwtInformation.accessToken(),
                jwtInformation.refreshToken()
            );
            log.debug("[InMemoryJwtRegistry] JwtInformation 등록 완료");
            return queue;
        });
    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {

        log.debug("[InMemoryJwtRegistry] JwtInformation 무효화 시작 - userId={}", userId);

        origin.computeIfPresent(userId, (id, q) -> {
            q.forEach(jwtInformation -> {
                removeTokenIndex(
                    jwtInformation.accessToken(),
                    jwtInformation.refreshToken()
                );
            });
            q.clear();
            log.debug("[InMemoryJwtRegistry] JwtInformation 무효화 완료 - userId={}", userId);
            return null;
        });
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        Queue<JwtInformation> q = origin.get(userId);
        return q != null && !q.isEmpty();
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        boolean result = accessTokenIndexes.contains(accessToken);
        if (!result) {
            log.debug("[InMemoryJwtRegistry] Access Token 유효성 검사 실패");

            for (String accessTokenIndex : accessTokenIndexes) {
                System.out.println("[ACCESS TOKEN 확인] : " + accessTokenIndex);
            }
        }
        return result;
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        return refreshTokenIndexes.contains(refreshToken);
    }

    @Override
    public boolean rotateJwtInformation(String refreshToken,
        JwtInformation newJwtInformation) {

        UUID userId = newJwtInformation.userDto().id();
        log.debug("[InMemoryJwtRegistry] Token Rotation 시작 - userId={}", userId);

        final boolean[] updated = {false};

        origin.computeIfPresent(userId, (id, q) -> {

            JwtInformation target = q.stream()
                .filter(jwtInformation -> jwtInformation.refreshToken().equals(refreshToken))
                .findFirst().orElse(null);

            if (target == null) {
                log.warn("[InMemoryJwtRegistry] Refresh 토큰 매칭 실패");
                return q;
            }

            removeTokenIndex(target.accessToken(), target.refreshToken());
            JwtInformation replaced = target.rotate(newJwtInformation.accessToken(),
                newJwtInformation.refreshToken());

            // 큐 교체
            q.remove(target);
            q.offer(replaced);

            addTokenIndex(newJwtInformation.accessToken(), newJwtInformation.refreshToken());

            updated[0] = true;
            log.debug("[InMemoryJwtRegistry] Token Rotation 완료 - userId={}", userId);
            return q;
        });

        return updated[0];
    }

    private void addTokenIndex(String accessToken, String refreshToken) {
        accessTokenIndexes.add(accessToken);
        refreshTokenIndexes.add(refreshToken);
    }

    private void removeTokenIndex(String accessToken, String refreshToken) {
        accessTokenIndexes.remove(accessToken);
        refreshTokenIndexes.remove(refreshToken);
    }
}
