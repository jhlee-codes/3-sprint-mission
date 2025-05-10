package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf")
public class JCFChannelRepository implements ChannelRepository {
    private final Map<UUID, Channel> data;

    public JCFChannelRepository() {
        this.data = new HashMap<>();
    }

    /**
     * 주어진 채널을 메모리에 저장
     *
     * @param channel 저장할 채널
     * @return 저장된 채널
     */
    @Override
    public Channel save(Channel channel) {
        this.data.put(channel.getId(), channel);
        return channel;
    }

    /**
     * 메모리에 저장되어있는 채널 데이터 리턴
     *
     * @return 저장된 채널데이터
     */
    @Override
    public List<Channel> findAll() {
        return this.data.values().stream().toList();
    }

    /**
     * 주어진 ID에 해당하는 채널 조회
     *
     * @param id 조회할 채널의 ID
     * @return 조회된 채널
     */
    @Override
    public Optional<Channel> findById(UUID id) {
        return Optional.ofNullable(this.data.get(id));
    }

    /**
     * 주어진 ID에 해당하는 채널의 존재여부 판단
     *
     * @param id 채널 ID
     * @return 해당 채널 존재여부
     */
    @Override
    public boolean existsById(UUID id) {
        return this.data.containsKey(id);
    }

    /**
     * 주어진 ID에 해당하는 채널 삭제
     *
     * @param id 삭제할 대상 채널 ID
     */
    @Override
    public void deleteById(UUID id) {
        this.data.remove(id);
    }
}
