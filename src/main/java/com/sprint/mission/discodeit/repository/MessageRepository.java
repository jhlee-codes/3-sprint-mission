package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {

    // 특정 채널 데이터 추가 후 저장
    public void save(Message message);
    // 데이터 전체 조회
    public Map<UUID, Message> findAll();
    // 데이터 단건 조회(id)
    public Optional<Message> findById(UUID messageId);
    // 데이터 단건 조회(메시지내용)
    public Optional<Message> findByMessageContent(String msgContent);
}
