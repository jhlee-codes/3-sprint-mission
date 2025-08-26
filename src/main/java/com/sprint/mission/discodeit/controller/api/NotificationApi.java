package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.dto.Notification.NotificationDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Tag(name = "Notification", description = "알림 API")
public interface NotificationApi {

    ResponseEntity<List<NotificationDto>> getMyNotifications(
        DiscodeitUserDetails me);

    ResponseEntity<Void> confirm(
        @Parameter(description = "확인할 Notification ID") UUID notificationId
    );
}
