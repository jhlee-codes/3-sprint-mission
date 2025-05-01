package com.sprint.mission.discodeit.repository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ChatRepository {

    // 특정 채팅 데이터 추가 후 저장
    public void save(UUID userId, UUID channelId);
    // 데이터 삭제
    public void delete(UUID userId, UUID channelId);
    // 데이터 전체 조회
    public Map<UUID, Set<UUID>> findAll();
    // 데이터 단건 조회 (유저ID)
    public Optional<Set<UUID>> findByUserId(UUID UserId);

}
