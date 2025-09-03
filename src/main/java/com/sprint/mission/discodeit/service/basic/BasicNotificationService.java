package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.annotation.Logging;
import com.sprint.mission.discodeit.dto.Notification.NotificationDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.base.BaseEntity;
import com.sprint.mission.discodeit.exception.Channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.Message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.Notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.exception.User.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Logging
@RequiredArgsConstructor
public class BasicNotificationService implements NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final NotificationMapper notificationMapper;
    private final CacheManager cacheManager;

    @Override
    @Cacheable(value = "user:notifications", key = "#receiverId")
    @Transactional(readOnly = true)
    public List<NotificationDto> findAllByReceiverId(UUID receiverId) {

        log.debug("[NotificationService] 전체 알림 조회 시작, receiverId = {}", receiverId);

        if (!userRepository.existsById(receiverId)) {
            throw UserNotFoundException.byId(receiverId);
        }

        List<Notification> notifications = notificationRepository.findAllByReceiverId(receiverId);

        log.debug("[NotificationService] 전체 알림 {}개 조회 완료", notifications.size());

        return notifications.stream()
            .map(notificationMapper::toDto)
            .toList();
    }

    @Override
    @Transactional
    public void delete(UUID notificationId) {

        log.debug("[NotificationService] 알림 확인 시작, ID = {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> {
                log.warn("알림 삭제 실패: 존재하지 않는 알림: ID = {}", notificationId);
                return new NotificationNotFoundException(notificationId);
            });
        UUID receiverId = notification.getReceiverId();

        notificationRepository.deleteById(notificationId);

        // 해당 사용자의 캐시 무효화
        Cache userNotificationsCache = cacheManager.getCache("user:notifications");
        if (userNotificationsCache != null) {
            userNotificationsCache.evictIfPresent(receiverId);
        }

        log.debug("[NotificationService] 알림 확인 완료, ID = {}", notificationId);
    }

    @Transactional(readOnly = true)
    public boolean isOwner(UUID notificationId, UUID userId) {
        return notificationRepository.existsByIdAndReceiverId(notificationId, userId);
    }

    @Override
    @Transactional
    public void createForNewMessage(UUID channelId, UUID messageId) {

        log.debug("[NotificationService] 새로운 메시지 알림 생성 시작, channelId = {}, messageId = {}",
            channelId, messageId);

        Channel ch = channelRepository.findById(channelId)
            .orElseThrow(() -> new ChannelNotFoundException(channelId));

        Message msg = messageRepository.findById(messageId)
            .orElseThrow(() -> new MessageNotFoundException(
                messageId));

        UUID authorId = msg.getAuthor().getId();

        Set<UUID> receiverIds = readStatusRepository.findAllByChannelIdAndNotificationEnabled(
                channelId, true
            ).stream().map(rs -> rs.getUser().getId())
            .filter(receiverId -> !receiverId.equals(authorId))
            .collect(Collectors.toSet());

        String title = ch.getName() == null ? msg.getAuthor().getUsername()
            : String.format("%s (#%s)", msg.getAuthor().getUsername(), ch.getName());
        String content = msg.getContent();

        create(receiverIds, title, content);
    }

    @Override
    @Transactional
    public void createForRoleUpdate(UUID userId, Role before, Role after) {

        log.debug("[NotificationService] 권한 변경 알림 생성 시작 - userId = {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));

        String title = "권한이 변경되었습니다.";
        String content = String.format("%s -> %s", before, after);

        create(Set.of(user.getId()), title, content);

        log.debug("[NotificationService] 권한 변경 알림 생성 완료 - userId = {}", userId);
    }

    @Override
    @Transactional
    public void createForS3UploadFailed(UUID binaryContentId, String requestId, String errorMsg) {

        log.debug("[NotificationService] S3 파일 업로드 실패 알림 생성 시작 - binaryContentId = {}",
            binaryContentId);

        Set<UUID> adminUserIds = userRepository.findAllByRole(Role.ADMIN)
            .stream().map(BaseEntity::getId)
            .collect(Collectors.toSet());

        String title = "S3 파일 업로드 실패";
        String content = String.format("RequestId: %s \n BinaryContentId: %s \n Error: %s",
            requestId, binaryContentId, errorMsg);

        create(adminUserIds, title, content);

        log.debug("[NotificationService] S3 파일 업로드 실패 알림 생성 완료 - binaryContentId = {}",
            binaryContentId);
    }

    /**
     * 알림을 생성, 저장하는 메서드
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void create(Set<UUID> receiverIds, String title, String content) {

        if (receiverIds.isEmpty()) {
            log.warn("[NotificationService] 알림 생성 요청이 비어있음: receiverIds={}", receiverIds);
            return;
        }

        List<Notification> notifications = receiverIds.stream()
            .map(receiverId -> Notification.builder()
                .receiverId(receiverId)
                .title(title)
                .content(content)
                .build())
            .toList();

        notificationRepository.saveAll(notifications);
        evictNotificationCache(receiverIds);
        log.debug("[NotificationService] 알림 {}개 생성 완료", notifications.size());
    }

    /* 알림 캐시를 무효화하는 메서드 */
    private void evictNotificationCache(Set<UUID> receiverIds) {

        Cache cache = cacheManager.getCache("user:notifications");
        if (cache != null) {
            receiverIds.forEach(cache::evictIfPresent);
            log.debug("[NotificationService] 알림 캐시 무효화 완료");
        } else {
            log.debug("[NotificationService] 알림 캐시가 존재하지 않음");
        }
    }
}
