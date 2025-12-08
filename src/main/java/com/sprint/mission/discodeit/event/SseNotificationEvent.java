package com.sprint.mission.discodeit.event;

import java.util.Collection;
import java.util.UUID;

public record SseNotificationEvent<T>(
    String topic,
    T payload,
    Collection<UUID> targetUserIds
) {

}
