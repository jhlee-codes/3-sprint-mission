package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    // 등록
    public Message createMessage(Channel sendChannel, User sendUser, String msgContent);
    // 모두 읽기
    public List<Message> getMessages();
    // 읽기
    public Message getMessage(UUID id);
    // 수정
    public Message updateMessage(Message message, String msgContent);
    // 삭제
    public Message deleteMessage(UUID id);

    // 메시지 내용으로 검색
    public Message searchContentByMessage(String msgContent);
}
