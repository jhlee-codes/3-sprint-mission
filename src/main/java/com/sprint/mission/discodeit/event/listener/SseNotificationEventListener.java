package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.event.SseNotificationEvent;
import com.sprint.mission.discodeit.event.kafka.producer.SseEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseNotificationEventListener {

    private final SseEventPublisher sseEventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleSseNotification(SseNotificationEvent<?> event) {
        log.debug("[SseEventListener] SSE 발행 이벤트 수신: topic={}", event.topic());

        if (event.targetUserIds() == null) {
            sseEventPublisher.publishSseBroadcast(event.topic(), event.payload());
            log.debug("[SseEventListener] 브로드캐스트 완료: topic={}", event.topic());
        } else {
            sseEventPublisher.publishSseTargeted(
                event.topic(),
                event.payload(),
                event.targetUserIds()
            );
            log.debug("[SseEventListener] 타겟 발행 완료: topic={}, targets={}", event.topic(),
                event.targetUserIds().size());
        }
    }
}
