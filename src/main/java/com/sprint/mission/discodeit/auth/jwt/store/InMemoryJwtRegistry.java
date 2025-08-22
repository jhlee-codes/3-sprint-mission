package com.sprint.mission.discodeit.auth.jwt.store;

import com.sprint.mission.discodeit.auth.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.dto.JwtInformation;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@RequiredArgsConstructor
public class InMemoryJwtRegistry implements JwtRegistry {

    private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
    private final Set<String> accessTokenIndexes = ConcurrentHashMap.newKeySet();
    private final Set<String> refreshTokenIndexes = ConcurrentHashMap.newKeySet();
    private final int maxActiveJwtCount;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {

        UUID userId = jwtInformation.getUserDto().id();
        log.debug("[InMemoryJwtRegistry] JwtInformation 등록 시작 - userId = {}", userId);

        origin.compute(userId, (id, q) -> {
            Queue<JwtInformation> queue = (q == null) ? new ConcurrentLinkedQueue<>() : q;

            while (queue.size() >= maxActiveJwtCount) {
                JwtInformation removed = queue.poll();
                if (removed != null) {
                    removeTokenIndex(
                        removed.getAccessToken(),
                        removed.getRefreshToken()
                    );
                }
            }

            queue.offer(jwtInformation);
            addTokenIndex(
                jwtInformation.getAccessToken(),
                jwtInformation.getRefreshToken()
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
                    jwtInformation.getAccessToken(),
                    jwtInformation.getRefreshToken()
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
        return accessTokenIndexes.contains(accessToken);
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        return refreshTokenIndexes.contains(refreshToken);
    }

    @Override
    public boolean rotateJwtInformation(String refreshToken,
        JwtInformation newJwtInformation) {

        UUID userId = newJwtInformation.getUserDto().id();
        log.debug("[InMemoryJwtRegistry] Token Rotation 시작 - userId={}", userId);

        final boolean[] updated = {false};

        origin.computeIfPresent(userId, (id, q) -> {

            JwtInformation target = q.stream()
                .filter(jwtInformation -> jwtInformation.getRefreshToken().equals(refreshToken))
                .findFirst().orElse(null);

            if (target == null) {
                log.warn("[InMemoryJwtRegistry] Refresh 토큰 매칭 실패");
                return q;
            }

            removeTokenIndex(target.getAccessToken(), target.getRefreshToken());
            target.rotate(newJwtInformation.getAccessToken(),
                newJwtInformation.getRefreshToken());

            addTokenIndex(newJwtInformation.getAccessToken(), newJwtInformation.getRefreshToken());

            updated[0] = true;
            log.debug("[InMemoryJwtRegistry] Token Rotation 완료 - userId={}", userId);
            return q;
        });

        return updated[0];
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Override
    public void clearExpiredJwtInformation() {
        log.debug("[InMemoryJwtRegistry] 주기적으로 만료된 토큰 정보 삭제 시작");

        origin.entrySet().removeIf(entry -> {
            Queue<JwtInformation> queue = entry.getValue();
            queue.removeIf(jwtInformation -> {
                boolean isExpired =
                    jwtTokenProvider.validateAccessToken(jwtInformation.getAccessToken()) ||
                        jwtTokenProvider.validateRefreshToken(jwtInformation.getRefreshToken());
                if (isExpired) {
                    removeTokenIndex(
                        jwtInformation.getAccessToken(),
                        jwtInformation.getRefreshToken()
                    );
                }
                return isExpired;
            });
            return queue.isEmpty(); // Remove the entry if the queue is empty
        });
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
