package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.event.UserLogInOutEvent;
import com.sprint.mission.discodeit.service.SseService;
import com.sprint.mission.discodeit.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserLogInOutEventListener {

    private final UserService userService;
    private final SseService sseService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserLogInOutEvent event) {

        UUID userId = event.userId();
        boolean isLogin = event.isLogin();
        log.debug("[UserLogInOutEventListener] 유저 로그인/로그아웃 이벤트 처리 시작 - userId={}, isLogin={}",
            userId, isLogin);

        UserDto userDto = userService.find(userId);
        sseService.broadcast("users.updated", userDto);
    }
}
