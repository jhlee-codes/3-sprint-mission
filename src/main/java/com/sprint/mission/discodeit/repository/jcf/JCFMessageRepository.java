package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf")
public class JCFMessageRepository implements MessageRepository {
    private final Map<UUID, Message> data;

    public JCFMessageRepository() {
        this.data = new HashMap<>();
    }

    /**
     * 주어진 메시지를 메모리에 저장
     *
     * @param message 저장할 메시지
     * @return 저장된 메시지
     */
    @Override
    public Message save(Message message) {
        this.data.put(message.getId(), message);
        return message;
    }

    /**
     * 메모리에 저장되어있는 메시지 데이터를 리턴
     *
     * @return 메모리에 저장된 메시지데이터
     */
    @Override
    public List<Message> findAll() {
        return this.data.values().stream().toList();
    }

    /**
     * 주어진 id에 해당하는 메시지 조회
     *
     * @param id 조회할 메시지의 ID
     * @return 조회된 메시지
     */
    @Override
    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(this.data.get(id));
    }

    /**
     * 주어진 채널ID에 해당하는 메시지 리스트 조회
     *
     * @param channelId 조회할 메시지의 채널ID
     * @return 조회된 메시지리스트
     */
    @Override
    public List<Message> findByChannelId(UUID channelId) {
        return data.values().stream()
                .filter(m->m.getChannelId().equals(channelId))
                .toList();
    }

    /**
     * 주어진 id에 해당하는 메시지의 존재여부 판단
     *
     * @param id 메시지 id
     * @return 해당 메시지 존재여부
     */
    @Override
    public boolean existsById(UUID id) {
        return this.data.containsKey(id);
    }

    /**
     * 주어진 id에 해당하는 메시지 삭제
     *
     * @param id 삭제할 대상 메시지 id
     */
    @Override
    public void deleteById(UUID id) {
        this.data.remove(id);
    }

}
