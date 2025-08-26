package com.sprint.mission.discodeit.event;

import java.util.UUID;

public record BinaryContentCreatedEvent(
    // 메타 정보가 DB에 잘 저장되었다는 사실을 의미하는 이벤트
    UUID binaryContentId,
    byte[] bytes
) {

}
