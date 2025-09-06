package com.sprint.mission.discodeit.event.listener.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserEventBroadcastTopicListener {

    private final ObjectMapper objectMapper;
    private final SseService sseService;

    @KafkaListener(
        topics = "discodeit.UserCreatedEvent",
        containerFactory = "broadcastKafkaListenerContainerFactory"
    )
    public void onUserCreatedEvent(String kafkaEvent) {
        log.debug(
            "[UserEventBroadcastTopicListener] UserCreatedEvent 토픽 구독 - SSE 브로드캐스팅 시작");
        processUserEvent(kafkaEvent, "user.created");
    }

    @KafkaListener(
        topics = "discodeit.UserUpdatedEvent",
        containerFactory = "broadcastKafkaListenerContainerFactory"
    )
    public void onUserUpdatedEvent(String kafkaEvent) {
        log.debug(
            "[UserEventBroadcastTopicListener] UserUpdatedEvent 토픽 구독 - SSE 브로드캐스팅 시작");
        processUserEvent(kafkaEvent, "user.updated");
    }

    @KafkaListener(
        topics = "discodeit.UserDeletedEvent",
        containerFactory = "broadcastKafkaListenerContainerFactory"
    )
    public void onUserDeletedEvent(String kafkaEvent) {
        log.debug(
            "[UserEventBroadcastTopicListener] UserDeletedEvent 토픽 구독 - SSE 브로드캐스팅 시작");
        processUserEvent(kafkaEvent, "user.deleted");
    }

    private void processUserEvent(String kafkaEvent, String eventName) {
        try {
            UserDto userDto = objectMapper.readValue(kafkaEvent, UserDto.class);
            sseService.broadcast(eventName, userDto);
        } catch (JsonProcessingException e) {
            log.error("[UserEventBroadcastTopicListener] UserDto 역직렬화 실패: {}",
                e.getMessage());
        }
    }
}
