package com.sprint.mission.discodeit.event.listener.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.Channel.ChannelDto;
import com.sprint.mission.discodeit.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChannelEventBroadcastTopicListener {

    private final ObjectMapper objectMapper;
    private final SseService sseService;

    @KafkaListener(
        topics = "discodeit.ChannelCreatedEvent",
        containerFactory = "broadcastKafkaListenerContainerFactory"
    )
    public void onChannelCreatedEvent(String kafkaEvent) {
        log.debug(
            "[ChannelEventBroadcastTopicListener] ChannelCreatedEvent 토픽 구독 - SSE 브로드캐스팅 시작");
        processChannelEvent(kafkaEvent, "channels.created");
    }

    @KafkaListener(
        topics = "discodeit.ChannelUpdatedEvent",
        containerFactory = "broadcastKafkaListenerContainerFactory"
    )
    public void onChannelUpdatedEvent(String kafkaEvent) {
        log.debug(
            "[ChannelEventBroadcastTopicListener] ChannelUpdatedEvent 토픽 구독 - SSE 브로드캐스팅 시작");
        processChannelEvent(kafkaEvent, "channels.updated");
    }

    @KafkaListener(
        topics = "discodeit.ChannelDeletedEvent",
        containerFactory = "broadcastKafkaListenerContainerFactory"
    )
    public void onChannelDeletedEvent(String kafkaEvent) {
        log.debug(
            "[ChannelEventBroadcastTopicListener] ChannelDeletedEvent 토픽 구독 - SSE 브로드캐스팅 시작");
        processChannelEvent(kafkaEvent, "channels.deleted");
    }

    private void processChannelEvent(String kafkaEvent, String eventName) {
        try {
            ChannelDto channelDto = objectMapper.readValue(kafkaEvent, ChannelDto.class);
            sseService.broadcast(eventName, channelDto);
        } catch (JsonProcessingException e) {
            log.error("[ChannelEventBroadcastTopicListener] ChannelDto 역직렬화 실패: {}",
                e.getMessage());
        }
    }
}
