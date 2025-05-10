package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStatusRepository {
    // 특정 UserStatus 데이터 추가 후 저장
    UserStatus save(UserStatus userStatus);
    // 데이터 전체 조회
    List<UserStatus> findAll();
    // 데이터 단건 조회 (id)
    Optional<UserStatus> findById(UUID id);
    // 데이터 단건 조회 (userId)
    Optional<UserStatus> findByUserId(UUID userId);
    // 데이터 존재여부 조회
    boolean existsById(UUID id);
    // 데이터 삭제 (id)
    void deleteById(UUID id);
}
