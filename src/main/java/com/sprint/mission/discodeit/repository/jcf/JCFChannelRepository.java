package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.*;

public class JCFChannelRepository implements ChannelRepository {
    private final Map<UUID, Channel> data;

    public JCFChannelRepository() {
        this.data = new HashMap<>();
    }

    public JCFChannelRepository(Map<UUID, Channel> data) {
        this.data = data;
    }

    /**
     * 주어진 채널을 메모리에 저장하는 메서드
     *
     * @param channel 저장할 채널
     */
    @Override
    public void save(Channel channel) {
        data.put(channel.getId(), channel);
    }

    /**
     * 주어진 id에 해당하는 채널을 삭제하는 메서드
     *
     * @param channelId 삭제할 대상 채널 id
     */
    @Override
    public void delete(UUID channelId) {
        data.remove(channelId);
    }

    /**
     * 메모리에 저장되어있는 채널 데이터를 리턴하는 메서드
     *
     * @return 메모리에 저장된 채널데이터
     */
    public Map<UUID, Channel> findAll() {
        return data;
    }

    /**
     * 주어진 id에 해당하는 채널을 조회하는 메서드
     *
     * @param channelId 조회할 채널의 ID
     * @return 조회된 채널
     */
    @Override
    public Optional<Channel> findById(UUID channelId) {
        return Optional.ofNullable(data.get(channelId));
    }

    /**
     * 주어진 채널명에 해당하는 채널을 조회하는 메서드
     *
     * @param channelName 조회할 채널명
     * @return 조회된 채널
     */
    @Override
    public Optional<Channel> findByChannelName(String channelName) {
        return data.values().stream()
                .filter(ch -> ch.getChannelName().equals(channelName))
                .findFirst();
    }
}
