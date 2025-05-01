package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ChannelRepository {

    // 특정 채널 데이터 추가 후 저장
    public void save(Channel channel);
    // 데이터 삭제
    public void delete(UUID channelId);
    // 데이터 전체 조회
    public Map<UUID, Channel> findAll();
    // 데이터 단건 조회 (id)
    public Optional<Channel> findById(UUID channelId);
    // 데이터 단건 조회 (채널이름)
    public Optional<Channel> findByChannelName(String channelName);
}
