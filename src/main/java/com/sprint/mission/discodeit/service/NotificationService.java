package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.Notification.NotificationDto;
import com.sprint.mission.discodeit.entity.Role;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface NotificationService {

    void create(Set<UUID> receiverIds, String title, String content);

    void createForNewMessage(UUID channelId, UUID messageId);

    void createForRoleUpdate(UUID userId, Role before, Role after);

    void createForS3UploadFailed(UUID binaryContentId, String requestId, String errorMsg);

    List<NotificationDto> findAllByReceiverId(UUID receiverId);

    void delete(UUID notificationId);
}
