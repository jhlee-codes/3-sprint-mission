package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {
    // 특정 채널 데이터 추가 후 저장
    Message save(Message message);
    // 데이터 전체 조회
    List<Message> findAll();
    // 데이터 단건 조회 0(id)
    Optional<Message> findById(UUID id);
    // 데이터 조회 (채널)
    List<Message> findByChannelId(UUID channelId);
    // 데이터 존재여부 조회
    boolean existsById(UUID id);
    // 데이터 삭제
    void deleteById(UUID id);
}
