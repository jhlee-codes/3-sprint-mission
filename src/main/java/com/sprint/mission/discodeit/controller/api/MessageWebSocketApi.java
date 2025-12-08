package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.Message.MessageCreateRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.Payload;

@Tag(name = "MessageWebSocket", description = "MessageWebSocket API")
public interface MessageWebSocketApi {

    void sendMessage(@Payload @Valid MessageCreateRequest request);
}
