package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class JCFMessageService implements MessageService {

    private final List<Message> data;

    public JCFMessageService(List<Message> data) {
        this.data = data;
    }

    @Override
    public Message createMessage(Channel sendChannel, User sendUser, String msgContent) {
        // 메시지 생성
        Message msg = new Message(sendChannel, sendUser, msgContent);
        // 메시지 컬렉션에 추가
        data.add(msg);
        // 채널의 메시지리스트에 메시지 추가
        sendChannel.updateMessageList(msg);
        return msg;
    }

    @Override
    public List<Message> getMessages() {
        return data;
    }

    @Override
    public Message getMessage(UUID id) {
        return data.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("해당 ID의 메시지가 존재하지 않습니다."));
    }

    @Override
    public Message updateMessage(Message msg, String msgContent) {
        // 메시지 유효성 검증
        if (msg == null || !data.contains(msg)) {
            throw new NoSuchElementException("존재하지 않는 메시지입니다.");
        }
        // 메시지 내용 업데이트
        for (Message m : data) {
            if (m.getId().equals(msg.getId())) {
                m.updateMsgContent(msgContent);
                return m;
            }
        }
        return null;
    }

    @Override
    public Message deleteMessage(UUID id) {
        Message targetMsg = getMessage(id);
        // 메시지 유효성 검증
        if (targetMsg == null || !data.contains(targetMsg)) {
            throw new NoSuchElementException("존재하지 않는 메시지이므로, 삭제가 불가합니다.");
        }
        Channel targetCh = targetMsg.getSendChannel();
        // 채널의 메시지리스트에서 해당 메시지 삭제
        targetCh.deleteMessageList(targetMsg);
        // 메시지 삭제
        data.remove(targetMsg);
        return targetMsg;
    }

    @Override
    public Message searchContentByMessage(String msgContent) {
        // 검색 결과가 여러 개인 경우, 가장 먼저 등록된 메시지를 조회
        // data를 순회하며 메시지 내용으로 검색
        for (Message msg : data) {
            if (msg.getMsgContent().equals(msgContent)) {
                return msg;
            }
        }
        throw new NoSuchElementException("해당 내용의 메시지를 찾을 수 없습니다.");
    }
}
