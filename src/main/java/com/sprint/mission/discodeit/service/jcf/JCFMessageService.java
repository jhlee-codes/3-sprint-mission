package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

public class JCFMessageService implements MessageService {
    private final Map<UUID, Message> data = new HashMap<>();

    /**
     * 주어진 채널, 유저, 메시지내용으로 메시지를 생성하는 메서드
     *
     * @param sendChannelId 메시지를 보낼 채널 ID
     * @param sendUserId 메시지를 보낸 유저 ID
     * @param msgContent 생성할 메시지 내용
     * @return 생성된 메시지
     */
    @Override
    public Message createMessage(UUID sendChannelId, UUID sendUserId, String msgContent) {
        // 메시지 생성
        Message msg = new Message(sendChannelId, sendUserId, msgContent);
        // 메시지 컬렉션에 추가
        data.put(msg.getId(), msg);
        return msg;
    }

    /**
     * 메모리에 저장되어있는 메시지 데이터를 리턴하는 메서드
     *
     * @return 메모리에 저장된 메시지데이터
     */
    @Override
    public Map<UUID, Message> getMessages() {
        return data;
    }

    /**
     * 주어진 id에 해당하는 메시지를 조회하는 메서드
     *
     * @param messageId 조회할 메시지의 ID
     * @return 조회된 메시지
     * @throws NoSuchElementException 해당 ID의 메시지가 존재하지 않는 경우
     */
    @Override
    public Message getMessageById(UUID messageId) {
        Message msg = data.get(messageId);
        if (msg == null) {
            throw new NoSuchElementException("해당 ID의 메시지가 존재하지 않습니다.");
        }
        return msg;
    }

    /**
     * 주어진 메시지내용에 해당하는 메시지를 조회하는 메서드
     *
     * @param msgContent 조회할 메시지내용
     * @return 조회된 메시지
     * @throws NoSuchElementException 해당 내용의 메시지가 존재하지 않는 경우
     */
    @Override
    public Message getMessageByContent(String msgContent) {
        // 검색 결과가 여러 개인 경우, 가장 먼저 등록된 메시지를 조회
        // data를 순회하며 메시지 내용으로 검색
        return data.values().stream()
                .filter(m->m.getMsgContent().equals(msgContent))
                .findFirst()
                .orElseThrow(()->new NoSuchElementException("해당 내용의 메시지를 찾을 수 없습니다."));
    }

    /**
     * 주어진 메시지를 새로운 메시지내용으로 수정하는 메서드
     *
     * @param messageId 수정할 대상 메시지 ID
     * @param msgContent 새로운 메시지내용
     * @return 수정된 메시지
     */
    @Override
    public Message updateMessage(UUID messageId, String msgContent) {
        Message targetMsg = getMessageById(messageId);
        // 메시지 내용 업데이트
        targetMsg.updateMsgContent(msgContent);
        return targetMsg;
    }

    /**
     * 주어진 id에 해당하는 메시지를 삭제하는 메서드
     *
     * @param messageId 삭제할 대상 메시지 id
     * @return 삭제된 메시지
     */
    @Override
    public Message deleteMessage(UUID messageId) {
        Message targetMsg = getMessageById(messageId);
        targetMsg.deleteMsgContent();
        return targetMsg;
    }

}
