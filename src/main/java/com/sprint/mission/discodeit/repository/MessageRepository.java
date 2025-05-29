package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.Message.MessageDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findAllByChannel_Id(UUID channelId);

    void deleteByChannel_Id(UUID channelId);

    Page<Message> findAllByChannel_IdOrderByCreatedAtDesc(UUID channelId, Pageable pageable);

    Optional<Message> findTopByChannel_IdOrderByCreatedAtDesc(UUID channelId);

    Slice<Message> findByChannel_IdAndCreatedAtBeforeOrderByCreatedAtDesc(UUID channelId,
            Instant createdAtBefore, Pageable pageable);

//    // 특정 채널 데이터 추가 후 저장
//    Message save(Message message);
//    // 데이터 전체 조회
//    List<Message> findAll();
//    // 데이터 단건 조회 (id)
//    Optional<Message> findById(UUID id);
//    // 데이터 조회 (채널)
//    List<Message> findByChannelId(UUID channelId);
//    // 데이터 존재여부 조회
//    boolean existsById(UUID id);
//    // 데이터 삭제
//    void deleteById(UUID id);
}
