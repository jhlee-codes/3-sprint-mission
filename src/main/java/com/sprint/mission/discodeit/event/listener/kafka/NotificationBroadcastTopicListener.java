package com.sprint.mission.discodeit.event.listener.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.Notification.NotificationDto;
import com.sprint.mission.discodeit.service.SseService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationBroadcastTopicListener {

    private final ObjectMapper objectMapper;
    private final SseService sseService;

    @KafkaListener(
        topics = "discodeit.NotificationCreatedEvent",
        containerFactory = "broadcastKafkaListenerContainerFactory"
    )
    public void onNotificationCreatedEvent(String kafkaEvent) {
        log.debug(
            "[NotificationBroadcastTopicListener] NotificationCreatedEvent 토픽 구독 - SSE 전송 시작");

        try {
            NotificationDto notificationDto = objectMapper.readValue(kafkaEvent,
                NotificationDto.class);

            sseService.send(
                Set.of(notificationDto.receiverId()),
                "notifications.created",
                notificationDto
            );
        } catch (JsonProcessingException e) {
            log.error("[NotificationBroadcastTopicListener] NotificationDto 역직렬화 실패: {}",
                e.getMessage());
        }
    }
}
