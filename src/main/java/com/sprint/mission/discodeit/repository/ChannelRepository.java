package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, UUID> {

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

    @Query("""
                SELECT c FROM Channel c
                WHERE c.type = 'PUBLIC'
                   OR c.id IN (
                       SELECT r.channel.id FROM ReadStatus r WHERE r.user.id = :userId
                   )
            """)
    List<Channel> findAllPublicOrUserChannels(UUID userId);
}
