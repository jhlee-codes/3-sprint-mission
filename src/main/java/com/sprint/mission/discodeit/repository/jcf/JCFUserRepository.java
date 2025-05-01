package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class JCFUserRepository implements UserRepository {
    private final Map<UUID, User> data;

    public JCFUserRepository() {
        this.data = new HashMap<>();
    }

    public JCFUserRepository(Map<UUID, User> data) {
        this.data = data;
    }

    /**
     * 유저를 메모리에 저장하는 메서드
     *
     * @param user 저장할 유저
     */
    @Override
    public void save(User user) {
        data.put(user.getId(), user);
    }

    /**
     * 주어진 id에 해당하는 유저를 삭제하는 메서드
     *
     * @param userId 삭제할 대상 유저 id
     */
    @Override
    public void delete(UUID userId) {
        data.remove(userId);
    }

    /**
     * 메모리에 저장되어있는 유저 데이터를 리턴하는 메서드
     *
     * @return 메모리에 저장된 유저데이터
     */
    public Map<UUID, User> findAll() {
        return data;
    }

    /**
     * 주어진 id에 해당하는 유저를 조회하는 메서드
     *
     * @param userId 조회할 유저의 id
     * @return 조회된 유저
     */
    @Override
    public Optional<User> findById(UUID userId) {
        return Optional.ofNullable(data.get(userId));
    }

    /**
     * 주어진 유저ID에 해당하는 유저를 조회하는 메서드
     *
     * @param loginId 조회할 유저ID
     * @return 조회된 유저
     */
    @Override
    public Optional<User> findByLoginId(String loginId) {
        return data.values().stream()
                .filter(u->u.getLoginId().equals(loginId))
                .findFirst();
    }
}
