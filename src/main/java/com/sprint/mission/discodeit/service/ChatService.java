package com.sprint.mission.discodeit.service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ChatService {
    // 채널 입장
    public void enterChannel(UUID userId, UUID channelId);
    // 채널 퇴장
    public void leaveChannel(UUID userId, UUID channelId);

    // 데이터 조회
    public Map<UUID, Set<UUID>> getUserChannelMap();
}

