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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Optional<Message> findTopByChannel_IdOrderByCreatedAtDesc(UUID channelId);

    void deleteAllByChannelId(UUID channelId);

    @Query("""
                SELECT m FROM Message m
                LEFT JOIN FETCH m.author a 
                JOIN FETCH a.status
                LEFT JOIN FETCH a.profile
                WHERE m.channel.id=:channelId AND m.createdAt < :createdAt
            """)
    Slice<Message> findAllByChannelIdWithAuthor(@Param("channelId") UUID channelId,
            @Param("createdAt") Instant createdAt,
            Pageable pageable);
}