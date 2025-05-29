package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.BinaryContent.BinaryContentDto;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public interface BinaryContentStorage {

    // 바이너리 데이터 저장/로드
    UUID put(UUID id, byte[] bytes);

    // 바이트 데이터 읽어 InputStream 타입으로 반환
    InputStream get(UUID id);

    // HTTP API 활용 다운로드 기능
    ResponseEntity<?> download(BinaryContentDto binaryContentDto);
}
