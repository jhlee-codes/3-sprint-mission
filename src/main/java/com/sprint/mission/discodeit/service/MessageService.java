package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;

import java.util.Map;
import java.util.UUID;

public interface MessageService {
    // 생성
    public Message createMessage(UUID sendChannelId, UUID sendUserId, String msgContent);
    // 전체 조회
    public Map<UUID, Message> getMessages();
    // 조회(ID)
    public Message getMessageById(UUID messageId);
    // 조회(내용)
    public Message getMessageByContent(String msgContent);
    // 수정
    public Message updateMessage(UUID messageId, String msgContent);
    // 삭제
    public Message deleteMessage(UUID messageId);

}
