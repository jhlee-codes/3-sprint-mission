package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.MessageWebSocketApi;
import com.sprint.mission.discodeit.dto.Message.MessageCreateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@RequiredArgsConstructor
@Controller
public class MessageWebSocketController implements MessageWebSocketApi {

    private final MessageService messageService;

    @MessageMapping("/messages")
    @Override
    public void sendMessage(@Payload @Valid MessageCreateRequest request) {
        log.debug("[MessageWebSocketController] 메시지 처리 시작");
        messageService.create(request, List.of());
        log.debug("[MessageWebSocketController] 메시지 처리 완료");
    }
}

