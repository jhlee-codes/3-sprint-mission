package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.Notification.NotificationDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;

@Tag(name = "Notification", description = "알림 API")
public interface NotificationApi {

    ResponseEntity<List<NotificationDto>> findAll();
}
