package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BinaryContentRepository {
    // 특정 Binary데이터 추가 후 저장
    BinaryContent save(BinaryContent binaryContent);
    // 데이터 전체 조회
    List<BinaryContent> findAll();
    // 데이터 단건 조회 (id)
    Optional<BinaryContent> findById(UUID id);
    // 데이터 존재여부 조회
    boolean existsById(UUID id);
    // 데이터 삭제 (id)
    void deleteById(UUID id);
}
