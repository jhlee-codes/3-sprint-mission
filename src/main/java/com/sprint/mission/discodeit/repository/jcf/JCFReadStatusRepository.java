package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf")
public class JCFReadStatusRepository implements ReadStatusRepository {
    private final Map<UUID, ReadStatus> data;

    public JCFReadStatusRepository() {
        this.data = new HashMap<>();
    }

    /**
     * 주어진 ReadStatus를 메모리에 저장
     *
     * @param readStatus 저장할 ReadStatus
     * @return 저장된 ReadStatus
     */
    @Override
    public ReadStatus save(ReadStatus readStatus) {
        this.data.put(readStatus.getId(), readStatus);
        return readStatus;
    }

    /**
     * 메모리에 저장되어있는 ReadStatus 데이터 리턴
     *
     * @return 저장된 ReadStatus 데이터
     */
    @Override
    public List<ReadStatus> findAll() {
        return this.data.values().stream().toList();
    }

    /**
     * 주어진 ID에 해당하는 ReadStatus 조회
     *
     * @param id 조회할 ReadStatus의 ID
     * @return 조회된 ReadStatus
     */
    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return Optional.ofNullable(this.data.get(id));
    }

    /**
     * 주어진 채널ID에 해당하는 ReadStatus 리스트 조회
     *
     * @param channelId 조회할 ReadStatus의 채널ID
     * @return 조회된 ReadStatus 리스트
     */
    @Override
    public List<ReadStatus> findByChannelId(UUID channelId) {
        return this.data.values().stream()
                .filter(rs -> rs.getChannelId().equals(channelId))
                .toList();
    }

    /**
     * 주어진 유저ID에 해당하는 ReadStatus 리스트 조회
     *
     * @param userId 조회할 ReadStatus의 유저ID
     * @return 조회된 ReadStatus 리스트
     */
    @Override
    public List<ReadStatus> findByUserId(UUID userId) {
        return this.data.values().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .toList();
    }

    /**
     * 주어진 채널Id에 해당하는 ReadStatus의 유저ID 리스트 조회
     *
     * @param channelId 조회할 채널Id
     * @return 조회된 유저ID 리스트
     */
    @Override
    public List<UUID> findUserIdByChannelId(UUID channelId) {
        return this.data.values().stream()
                .filter(rs -> rs.getChannelId().equals(channelId))
                .map(rs -> rs.getUserId())
                .toList();
    }

    /**
     * 주어진 ID에 해당하는 ReadStatus 존재여부 판단
     *
     * @param id ReadStatus ID
     * @return 해당 ReadStatus 존재여부
     */
    @Override
    public boolean existsById(UUID id) {
        return this.data.containsKey(id);
    }

    /**
     * 주어진 채널ID, 유저ID에 해당하는 ReadStatus 존재여부 판단
     *
     * @param channelId 채널ID
     * @param userId 유저ID
     * @return 해당 ReadStatus 존재여부
     */
    @Override
    public boolean existsByChannelIdAndUserId(UUID channelId, UUID userId) {
        return this.data.values().stream()
                .anyMatch(rs->rs.getChannelId().equals(channelId) && rs.getUserId().equals(userId));
    }

    /**
     * 주어진 ID에 해당하는 ReadStatus 삭제
     *
     * @param id 삭제할 대상 ReadStatus ID
     */
    @Override
    public void deleteById(UUID id) {
        this.data.remove(id);
    }
}
