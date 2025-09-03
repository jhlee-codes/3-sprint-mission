package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.Message.MessageDto;
import java.util.UUID;

public record MessageCreatedEvent(
    // 새로운 메시지가 등록된 사실을 의미하는 이벤트
    MessageDto messageDto,
    UUID channelId,
    UUID messageId
) {

}
