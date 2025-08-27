package com.sprint.mission.discodeit.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3FileUploadFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProduceRequiredEventListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Async("notificationTaskExecutor")
    @TransactionalEventListener
    public void on(MessageCreatedEvent event) {

        log.debug("[KafkaProduceRequiredEventListener] MessageCreatedEvent Kafka로 발행");
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("discodeit.MessageCreatedEvent", payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Async("notificationTaskExecutor")
    @TransactionalEventListener
    public void on(RoleUpdatedEvent event) {

        log.debug("[KafkaProduceRequiredEventListener] RoleUpdatedEvent Kafka로 발행");
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("discodeit.RoleUpdatedEvent", payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Async("notificationTaskExecutor")
    @EventListener
    public void on(S3FileUploadFailedEvent event) {

        log.debug("[KafkaProduceRequiredEventListener] S3FileUploadFailedEvent Kafka로 발행");
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("discodeit.S3FileUploadFailedEvent", payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
