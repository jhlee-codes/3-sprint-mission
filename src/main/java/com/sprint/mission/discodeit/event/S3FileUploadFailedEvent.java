package com.sprint.mission.discodeit.event;

import java.util.UUID;

public record S3FileUploadFailedEvent(
    // S3 파일 업로드가 실패되었다는 사실을 의미하는 이벤트
    UUID binaryContentId,
    String requestId,
    String errorMsg
) {

}
