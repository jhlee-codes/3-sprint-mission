package com.sprint.mission.discodeit.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.event.UserLogInOutEvent;
import com.sprint.mission.discodeit.service.SseService;
import com.sprint.mission.discodeit.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserLogInOutEventListener {

    private final UserService userService;
    private final SseService sseService;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @EventListener
    public void on(UserLogInOutEvent event) {

        UUID userId = event.userId();
        boolean isLogin = event.isLogin();
        log.debug("[UserLogInOutEventListener] 유저 로그인/로그아웃 이벤트 처리 시작 - userId={}, isLogin={}",
            userId, isLogin);

        UserDto userDto = userService.find(userId);

        // Kafka 이벤트 발행
        publishKafkaEvent(userDto);
    }

    private void publishKafkaEvent(UserDto userDto) {
        try {
            String payload = objectMapper.writeValueAsString(userDto);
            String topic = "discodeit.UserUpdatedEvent";
            kafkaTemplate.send(topic, payload);
            log.debug("[UserLogInOutEventListener] SSE 푸시 Kafka 이벤트 발행 완료: {}", payload);
        } catch (JsonProcessingException e) {
            log.error("[UserLogInOutEventListener] UserDto 직렬화 실패: {}",
                e.getMessage());
        }
    }
}
