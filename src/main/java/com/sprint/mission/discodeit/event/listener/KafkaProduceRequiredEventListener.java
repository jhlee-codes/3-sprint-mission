package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3FileUploadFailedEvent;
import com.sprint.mission.discodeit.event.kafka.producer.KafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProduceRequiredEventListener {

    private final KafkaEventPublisher kafkaEventPublisher;

    @Async("notificationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageCreated(MessageCreatedEvent event) {

        log.debug("[KafkaProduceRequiredEventListener] MessageCreatedEvent Kafka로 발행");
        kafkaEventPublisher.publish("discodeit.MessageCreatedEvent", event);
    }

    @Async("notificationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoleUpdated(RoleUpdatedEvent event) {

        log.debug("[KafkaProduceRequiredEventListener] RoleUpdatedEvent Kafka로 발행");
        kafkaEventPublisher.publish("discodeit.RoleUpdatedEvent", event);
    }

    @Async("notificationTaskExecutor")
    @TransactionalEventListener
    public void handleS3FileUploadFailed(S3FileUploadFailedEvent event) {

        log.debug("[KafkaProduceRequiredEventListener] S3FileUploadFailedEvent Kafka로 발행");
        kafkaEventPublisher.publish("discodeit.S3FileUploadFailedEvent", event);
    }
}
