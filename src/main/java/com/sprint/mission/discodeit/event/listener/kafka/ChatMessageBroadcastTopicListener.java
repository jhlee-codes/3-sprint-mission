package com.sprint.mission.discodeit.event.listener.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatMessageBroadcastTopicListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
        topics = "discodeit.MessageCreatedEvent",
        containerFactory = "broadcastKafkaListenerContainerFactory"
    )
    public void onMessageCreatedEvent(String kafkaEvent) {
        log.debug("[ChatMessageBroadcastTopicListener] MessageCreatedEvent 토픽 구독 - 메시지 브로드캐스팅 시작");

        try {
            MessageCreatedEvent event = objectMapper.readValue(kafkaEvent,
                MessageCreatedEvent.class);
            String dest = "/sub/channels." + event.channelId() + ".messages";

            messagingTemplate.convertAndSend(dest, event.messageDto());
            log.debug("[ChatMessageBroadcastTopicListener] STOMP 메시지 브로드캐스팅 완료: dest={}", dest);
        } catch (JsonProcessingException e) {
            log.error("[ChatMessageBroadcastTopicListener] 메시지 역직렬화 실패: {}", e.getMessage());
        }
    }
}
