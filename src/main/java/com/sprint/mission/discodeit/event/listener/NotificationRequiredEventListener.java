package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.Channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.Message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.User.UserNotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

    private final AuthService authService;
    private final ReadStatusRepository readStatusRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(MessageCreatedEvent event) {

        UUID channelId = event.channelId();
        UUID messageId = event.messageId();

        Channel ch = channelRepository.findById(channelId)
            .orElseThrow(() -> new ChannelNotFoundException(channelId));

        Message msg = messageRepository.findById(messageId)
            .orElseThrow(() -> new MessageNotFoundException(
                messageId));

        UUID authorId = msg.getAuthor().getId();
        List<User> receivers = readStatusRepository.findByChannel_IdAndNotificationEnabled(
                channelId, true).stream()
            .map(ReadStatus::getUser)
            .filter(u -> !u.getId().equals(authorId))
            .distinct()
            .toList();

        if (receivers.isEmpty()) {
            log.debug("[NotificationRequiredEventListener] 메시지 알림 수신자가 없습니다. 채널 ID = {}",
                channelId);
            return;
        }

        String title = ch.getName() == null ? msg.getAuthor().getUsername()
            : String.format("%s (#%s)", msg.getAuthor().getUsername(), ch.getName());
        String content = msg.getContent();

        List<Notification> notifications = receivers.stream()
            .map(user -> Notification.builder()
                .receiver(user)
                .title(title)
                .content(content)
                .build())
            .toList();

        notificationRepository.saveAll(notifications);
        log.debug("[NotificationRequiredEventListener] 메시지 알림 {}개 생성 완료", notifications.size());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(RoleUpdatedEvent event) {

        UUID userId = event.userId();

        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));

        String title = "권한이 변경되었습니다.";
        String content = String.format("%s -> %s", event.beforeRole(), event.afterRole());

        Notification notification = Notification.builder()
            .receiver(user)
            .title(title)
            .content(content)
            .build();

        notificationRepository.save(notification);
        log.debug("[NotificationRequiredEventListener] 권한 변경 알림 생성 완료 - userId = {}", userId);
    }
}
