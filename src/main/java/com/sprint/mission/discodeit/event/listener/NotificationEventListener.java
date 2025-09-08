package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.Notification.NotificationDto;
import com.sprint.mission.discodeit.event.NotificationsCreatedEvent;
import com.sprint.mission.discodeit.event.kafka.producer.SseEventPublisher;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationEventListener {

    private final CacheManager cacheManager;
    private final SseEventPublisher sseEventPublisher;

    @Async("notificationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationsCreated(NotificationsCreatedEvent e) {
        // 캐시 무효화 (커밋 이후)
        Cache cache = cacheManager.getCache("user:notifications");
        if (cache != null) {
            e.receiverIds().forEach(cache::evictIfPresent);
            log.debug("[NotificationEventListener] AFTER_COMMIT 캐시 무효화 완료: {}", e.receiverIds());
        }

        List<NotificationDto> notificationDtos = e.notificationDtos();

        notificationDtos.forEach(dto -> {
            sseEventPublisher.publishSseTargeted(
                "notifications.created",
                dto,
                Set.of(dto.receiverId())
            );
        });
    }
}
