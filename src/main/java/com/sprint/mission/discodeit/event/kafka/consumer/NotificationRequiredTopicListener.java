package com.sprint.mission.discodeit.event.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3FileUploadFailedEvent;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredTopicListener {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @KafkaListener(topics = "discodeit.MessageCreatedEvent")
    public void onMessageCreatedEvent(String kafkaEvent) {

        log.debug("[NotificationRequiredTopicListener] MessageCreatedEvent 토픽 구독 - 알림 생성 시작");
        try {
            MessageCreatedEvent event = objectMapper.readValue(kafkaEvent,
                MessageCreatedEvent.class);
            UUID channelId = event.channelId();
            UUID messageId = event.messageId();

            notificationService.createForNewMessage(channelId, messageId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "discodeit.RoleUpdatedEvent")
    public void onRoleUpdatedEvent(String kafkaEvent) {

        log.debug("[NotificationRequiredTopicListener] RoleUpdatedEvent 토픽 구독 - 알림 생성 시작");
        try {
            RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);
            UUID userId = event.userId();
            Role before = event.beforeRole();
            Role after = event.afterRole();

            notificationService.createForRoleUpdate(userId, before, after);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "discodeit.S3FileUploadFailedEvent")
    public void onS3UploadFailedEvent(String kafkaEvent) {

        log.debug("[NotificationRequiredTopicListener] S3FileUploadFailedEvent 토픽 구독 - 알림 생성 시작");
        try {
            S3FileUploadFailedEvent event = objectMapper.readValue(kafkaEvent,
                S3FileUploadFailedEvent.class);
            UUID binaryContentId = event.binaryContentId();
            String requestId = event.requestId();
            String errorMsg = event.errorMsg();

            notificationService.createForS3UploadFailed(binaryContentId, requestId, errorMsg);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
