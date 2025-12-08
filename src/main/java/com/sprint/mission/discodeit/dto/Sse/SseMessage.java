package com.sprint.mission.discodeit.dto.Sse;

import java.util.Set;
import java.util.UUID;

public record SseMessage(
    UUID id,
    String eventName,
    Object event,
    Set<UUID> receiverIds
) {

}
