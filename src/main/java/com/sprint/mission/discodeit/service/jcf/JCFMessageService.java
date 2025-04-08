package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.List;
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
        System.out.println("메시지 생성 ) \"" + msg.getMsgContent() + "\" 등록되었습니다.");
        return msg;
    }

    @Override
    public List<Message> getMessages() {
        return data;
    }

    @Override
    public Message getMessage(UUID id) {
        return data.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public void updateMessage(Message msg, String msgContent) {
        // 메시지 유효성 검증
        if (msg == null || !data.contains(msg)) {
            System.out.println("존재하지 않는 메시지므로, 수정이 불가합니다.");
            return ;
        }

        String beforeMsgContent = msg.getMsgContent();

        // 메시지 내용 업데이트
        for (Message m : data) {
            if (m.getId().equals(msg.getId())) {
                m.updateMsgContent(msgContent);
                break;
            }
        }
        System.out.println("메시지 수정 ) \"" + beforeMsgContent +"\" -> \"" + msgContent + "\" 수정되었습니다.");
    }

    @Override
    public void deleteMessage(UUID id) {
        Message targetMsg = getMessage(id);

        // 메시지 유효성 검증
        if (targetMsg == null || !data.contains(targetMsg)) {
            System.out.println("존재하지 않는 메시지므로, 삭제가 불가합니다.");
            return ;
        }

        Channel targetCh = targetMsg.getSendChannel();

        // 채널의 메시지리스트에서 해당 메시지 삭제
        targetCh.deleteMessageList(targetMsg);

        // 메시지 삭제
        data.remove(targetMsg);
        System.out.println("메시지 삭제 ) \"" + targetMsg.getMsgContent() + "\" 삭제되었습니다.");
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
        return null;
    }
}
