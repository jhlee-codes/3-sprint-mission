package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.Notification.NotificationDto;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record NotificationsCreatedEvent(
    Set<UUID> receiverIds,
    List<NotificationDto> notificationDtos
) {

}
