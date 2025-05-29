package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.ReadStatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

    void deleteAllByChannel_Id(UUID channelId);

    boolean existsByChannel_IdAndUser_Id(UUID channelId, UUID userId);

    List<ReadStatus> findAllByUser_Id(UUID userId);

    Optional<ReadStatus> findById(UUID id);

    List<ReadStatus> findAllByChannel_Id(UUID channelId);

//
//    // 특정 ReadStatus 데이터 추가 후 저장
//    ReadStatus save(ReadStatus readStatus);
//
//    // 데이터 전체 조회
//    List<ReadStatus> findAll();
//
//    // 데이터 단건 조회 (id)
//    Optional<ReadStatus> findById(UUID id);
//
//    // 데이터 조회 (채널)
//    List<ReadStatus> findByChannelId(UUID channelId);
//
//    // 데이터 조회 (유저)
//    List<ReadStatus> findByUserId(UUID userId);
//
//    // 채널ID와 매핑되는 UserId 조회
//    List<UUID> findUserIdByChannelId(UUID channelId);
//
//    // 데이터 존재여부 조회 (id)
//    boolean existsById(UUID id);
//
//    // 데이터 존재여부 조회 (채널id, 유저id)
//    boolean existsByChannelIdAndUserId(UUID channelId, UUID userId);
//
//    // 데이터 삭제
//    void deleteById(UUID id);

}
