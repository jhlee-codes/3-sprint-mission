package com.sprint.mission.discodeit.exception.Notification;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class NotificationNotFoundException extends NotificationException {

    private final UUID notificationId;

    public NotificationNotFoundException(UUID notificationId) {
        super(
            ErrorCode.NOTIFICATION_NOT_FOUND,
            Map.of("notificationId", notificationId)
        );
        this.notificationId = notificationId;
    }
}
