package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;

    public BasicMessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

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
        messageRepository.save(msg);
        return msg;
    }

    /**
     * 레포지토리에서 읽어온 메시지 데이터를 리턴하는 메서드
     *
     * @return 저장된 메시지 데이터
     */
    @Override
    public Map<UUID, Message> getMessages() {
        return messageRepository.findAll();
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
        return messageRepository.findById(messageId)
                .orElseThrow(()->new NoSuchElementException("해당 ID의 메시지가 존재하지 않습니다."));
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
        return messageRepository.findByMessageContent(msgContent)
                .orElseThrow(()->new NoSuchElementException("해당 내용의 메시지를 찾을 수 없습니다."));
    }

    /**
     * 주어진 메시지를 새로운 메시지내용으로 수정하는 메서드
     *
     * @param messageId 수정할 대상 메시지
     * @param msgContent 새로운 메시지내용
     * @return 수정된 메시지
     */
    @Override
    public Message updateMessage(UUID messageId, String msgContent) {
        Message targetMsg = getMessageById(messageId);
        // 메시지 내용 업데이트
        targetMsg.updateMsgContent(msgContent);
        messageRepository.save(targetMsg);
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
        // 메시지 삭제
        targetMsg.deleteMsgContent();
        messageRepository.save(targetMsg);
        return targetMsg;
    }

}
