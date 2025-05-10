package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserStatus.UserStatusCreateRequestDTO;
import com.sprint.mission.discodeit.dto.UserStatus.UserStatusUpdateRequestDTO;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {
    // 생성
    UserStatus create(UserStatusCreateRequestDTO createRequestDTO);
    // 전체 조회
    List<UserStatus> findAll();
    // 조회 (id)
    UserStatus find(UUID id);
    // 조회 (userId)
    UserStatus findByUserId(UUID userId);
    // 수정
    UserStatus update(UUID userStatusId, UserStatusUpdateRequestDTO updateRequestDTO);
    // 수정 (userId)
    UserStatus updateByUserId(UUID userId, UserStatusUpdateRequestDTO updateRequestDTO);
    // 삭제
    void delete(UUID id);
}
