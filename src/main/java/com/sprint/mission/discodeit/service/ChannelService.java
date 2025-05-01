package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.Map;
import java.util.UUID;

public interface ChannelService {
    // 생성
    public Channel createChannel(String channelName);
    // 전체 조회
    public Map<UUID, Channel> getChannels();
    // 조회(ID)
    public Channel getChannelById(UUID channelId);
    // 조회(채널명)
    public Channel getChannelByChannelName(String channelName);
    // 수정
    public Channel updateChannel(UUID channelId, String channelName);
    // 삭제
    public Channel deleteChannel(UUID channelId);

}
