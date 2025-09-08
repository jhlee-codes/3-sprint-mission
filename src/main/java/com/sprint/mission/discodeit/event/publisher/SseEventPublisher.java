package com.sprint.mission.discodeit.event.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.Sse.SseMessage;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseEventPublisher {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String SSE_BROADCAST_TOPIC = "discodeit.SseBroadcastEvent";

    public void publishSseBroadcast(String eventName, Object data) {
        SseMessage sseMessage = new SseMessage(UUID.randomUUID(), eventName, data, null);
        publishToSseBroadcastTopic(sseMessage);
    }

    public void publishSseTargeted(String eventName, Object data, Collection<UUID> receiverIds) {
        SseMessage sseMessage = new SseMessage(UUID.randomUUID(), eventName, data,
            new HashSet<>(receiverIds));
        publishToSseBroadcastTopic(sseMessage);
    }

    private void publishToSseBroadcastTopic(SseMessage sseMessage) {
        try {
            String payload = objectMapper.writeValueAsString(sseMessage);
            kafkaTemplate.send(SSE_BROADCAST_TOPIC, payload);
            log.debug("[SseEventPublisher] SSE 브로드캐스트 토픽 발행 완료: eventName={}",
                sseMessage.eventName());
        } catch (JsonProcessingException e) {
            log.error("[SseEventPublisher] SseKafkaMessage 직렬화 실패: {}", e.getMessage());
        }
    }
}
