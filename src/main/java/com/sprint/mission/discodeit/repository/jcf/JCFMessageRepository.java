package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class JCFMessageRepository implements MessageRepository {
    private final Map<UUID, Message> data;

    public JCFMessageRepository() {
        this.data = new HashMap<>();
    }

    public JCFMessageRepository(Map<UUID, Message> data) {
        this.data = data;
    }

    /**
     * 주어진 메시지를 메모리에 저장하는 메서드
     *
     * @param message 저장할 메시지
     */
    @Override
    public void save(Message message) {
        data.put(message.getId(), message);
    }

    /**
     * 메모리에 저장되어있는 메시지 데이터를 리턴하는 메서드
     *
     * @return 메모리에 저장된 메시지데이터
     */
    public Map<UUID, Message> findAll() {
        return data;
    }

    /**
     * 주어진 id에 해당하는 메시지를 조회하는 메서드
     *
     * @param messageId 조회할 메시지의 ID
     * @return 조회된 메시지
     */
    @Override
    public Optional<Message> findById(UUID messageId) {
        return Optional.ofNullable(data.get(messageId));
    }

    /**
     * 주어진 메시지내용에 해당하는 메시지를 조회하는 메서드
     *
     * @param msgContent 조회할 메시지내용
     * @return 조회된 메시지
     */
    @Override
    public Optional<Message> findByMessageContent(String msgContent) {
        return data.values().stream()
                .filter(m->m.getMsgContent().equals(msgContent))
                .findFirst();
    }
}
