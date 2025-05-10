package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.Channel.*;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    // 생성 (Public)
    Channel create(PublicChannelCreateRequestDTO createRequestDTO);
    // 생성 (Private)
    Channel create(PrivateChannelCreateRequestDTO createRequestDTO);
    // 전체 조회
    List<ChannelDTO> findAllByUserId(UUID userId);
    // 조회(ID)
    ChannelDTO find(UUID channelId);
    // 수정
    Channel update(UUID channelId, PublicChannelUpdateRequestDTO updateRequestDTO);
    // 삭제
    void delete(UUID channelId);
}
