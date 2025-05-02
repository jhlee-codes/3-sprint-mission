package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf")
public class JCFUserStatusRepository implements UserStatusRepository {
    private final Map<UUID, UserStatus> data;

    public JCFUserStatusRepository() {
        this.data = new HashMap<>();
    }

    /**
     * 주어진 UserStatus를 메모리에 저장
     *
     * @param userStatus 저장할 UserStatus
     * @return 저장된 UserStatus
     */
    @Override
    public UserStatus save(UserStatus userStatus) {
        this.data.put(userStatus.getId(),userStatus);
        return userStatus;
    }

    /**
     * 메모리에 저장되어있는 UserStatus 데이터를 리턴
     *
     * @return 메모리에 저장된 UserStatus 데이터
     */
    @Override
    public List<UserStatus> findAll() {
        return this.data.values().stream().toList();
    }

    /**
     * 주어진 id에 해당하는 UserStatus 조회
     *
     * @param id 조회할 UserStatus id
     * @return 조회된 UserStatus
     */
    @Override
    public Optional<UserStatus> findById(UUID id) {
        return Optional.ofNullable(this.data.get(id));
    }

    /**
     * 주어진 유저ID에 해당하는 UserStatus 조회
     *
     * @param userId 조회할 UserStatus의 유저ID
     * @return 조회된 UserStatus
     */
    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return this.data.values().stream()
                .filter(us -> us.getUserId().equals(userId))
                .findFirst();
    }

    /**
     * 주어진 id에 해당하는 UserStatus 존재여부 판단
     *
     * @param id UserStatus id
     * @return 해당 UserStatus 존재여부
     */
    @Override
    public boolean existsById(UUID id) {
        return this.data.containsKey(id);
    }

    /**
     * 주어진 id에 해당하는 UserStatus 삭제
     *
     * @param id 삭제할 대상 UserStatus ID
     */
    @Override
    public void deleteById(UUID id) {
        this.data.remove(id);
    }
}
