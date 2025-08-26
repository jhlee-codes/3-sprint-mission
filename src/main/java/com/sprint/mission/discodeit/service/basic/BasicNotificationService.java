package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.annotation.Logging;
import com.sprint.mission.discodeit.dto.Notification.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.exception.User.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Logging
@RequiredArgsConstructor
public class BasicNotificationService implements NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> findAllByReceiverId(UUID receiverId) {

        log.debug("[NotificationService] 전체 알림 조회 요청, receiverId = {}", receiverId);

        if (!userRepository.existsById(receiverId)) {
            throw UserNotFoundException.byId(receiverId);
        }

        List<Notification> notifications = notificationRepository.findAllByReceiverId(receiverId);

        return notifications.stream()
            .map(notificationMapper::toDto)
            .toList();
    }
}
