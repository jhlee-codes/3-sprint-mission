package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.event.SseNotificationEvent;
import com.sprint.mission.discodeit.event.UserLogInOutEvent;
import com.sprint.mission.discodeit.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserLogInOutEventListener {

    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @EventListener
    public void handleUserLoginOut(UserLogInOutEvent event) {

        UUID userId = event.userId();
        boolean isLogin = event.isLogin();
        log.debug("[UserLogInOutEventListener] 유저 로그인/로그아웃 이벤트 처리 시작 - userId={}, isLogin={}",
            userId, isLogin);

        UserDto userDto = userService.find(userId);

        eventPublisher.publishEvent(new SseNotificationEvent<>("users.updated", userDto, null));
    }
}
