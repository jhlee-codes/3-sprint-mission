package com.sprint.mission.discodeit.event.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.Sse.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import com.sprint.mission.discodeit.service.SseService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseBroadcastKafkaListener {

    private final ObjectMapper objectMapper;
    private final SseEmitterRepository emitterRepository;
    private final SseMessageRepository messageRepository;
    private final SseService sseService;

    @KafkaListener(
        topics = "discodeit.SseBroadcastEvent",
        containerFactory = "broadcastKafkaListenerContainerFactory"
    )
    public void onSseBroadcastEvent(String kafkaEvent) {
        log.debug("[SseBroadcastKafkaListener] SSE 브로드캐스트 이벤트 수신: {}", kafkaEvent);

        try {
            SseMessage msg = objectMapper.readValue(kafkaEvent, SseMessage.class);
            Set<UUID> receiverIds = msg.receiverIds();
            messageRepository.save(msg);

            if (receiverIds == null || receiverIds.isEmpty()) {
                Map<UUID, List<SseEmitter>> allEmitters = emitterRepository.findAll();
                log.debug("[SseBroadcastKafkaListener] 전체 브로드캐스트 시도. findAll() 맵 크기: {}",
                    allEmitters.size());

                sseService.broadcast(msg.eventName(), msg.event());

            } else {
                log.debug("[SseBroadcastKafkaListener] 타겟 브로드캐스트 시도. 대상 receiverIds: {}",
                    receiverIds);

                sseService.send(receiverIds, msg.eventName(), msg.event());
            }
        } catch (JsonProcessingException e) {
            log.error("[SseBroadcastKafkaListener] SseKafkaMessage 역직렬화 실패: {}", e.getMessage());
        }
    }
}
