package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Role;
import java.util.UUID;

public record RoleUpdatedEvent(
    // 사용자의 권한이 변경된 사실을 의미하는 이벤트
    UUID userId,
    Role beforeRole,
    Role afterRole
) {

}
