package com.sprint.mission.discodeit.event.listener.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.BinaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BinaryContentEventBroadcastTopicListener {

    private final ObjectMapper objectMapper;
    private final SseService sseService;

    @KafkaListener(
        topics = "discodeit.BinaryContentUpdatedEvent",
        containerFactory = "broadcastKafkaListenerContainerFactory"
    )
    public void onBinaryContentUpdatedEvent(String kafkaEvent) {
        log.debug(
            "[BinaryContentEventBroadcastTopicListener] BinaryContentUpdatedEvent 토픽 구독 - SSE 전송 시작");

        try {
            BinaryContentDto binaryContentDto = objectMapper.readValue(kafkaEvent,
                BinaryContentDto.class);

            sseService.broadcast("binaryContents.updated", binaryContentDto);

        } catch (JsonProcessingException e) {
            log.error("[BinaryContentEventBroadcastTopicListener] BinaryContentDto 역직렬화 실패: {}",
                e.getMessage());
        }
    }
}
