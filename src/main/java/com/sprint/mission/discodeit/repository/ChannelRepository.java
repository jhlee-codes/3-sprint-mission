package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelRepository {
    // 특정 채널 데이터 추가 후 저장
    Channel save(Channel channel);
    // 데이터 전체 조회
    List<Channel> findAll();
    // 데이터 단건 조회 (id)
    Optional<Channel> findById(UUID id);
    // 데이터 존재여부 조회 (id)
    boolean existsById(UUID id);
    // 데이터 삭제
    void deleteById(UUID id);
}
