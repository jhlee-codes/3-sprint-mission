package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    // 생성
    public Channel createChannel(String channelName);
    // 읽기
    public Channel getChannel(UUID id);
    // 모두 읽기
    public List<Channel> getChannels();
    // 수정
    public Channel updateChannel(Channel channel, String channelName);
    // 삭제
    public Channel deleteChannel(UUID id);

    // 채널 입장
    public Channel enterChannel(User user, Channel channel);
    // 채널 퇴장
    public Channel leaveChannel(User user, Channel channel);

    // 채널 이름으로 검색
    public Channel searchChannelByChannelName(String channelName);
}
