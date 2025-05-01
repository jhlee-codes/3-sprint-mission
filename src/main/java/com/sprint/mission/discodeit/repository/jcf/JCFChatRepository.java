package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.repository.ChatRepository;

import java.util.*;

public class JCFChatRepository implements ChatRepository {
    private final Map<UUID, Set<UUID>> data;

    public JCFChatRepository() {
        this.data = new HashMap<>();
    }

    public JCFChatRepository(Map<UUID, Set<UUID>> data) {
        this.data = data;
    }

    /**
     * 주어진 데이터를 메모리에 저장하는 메서드
     *
     * @param userId 저장할 유저 ID
     * @param channelId 저장할 채널 ID
     */
    @Override
    public void save(UUID userId, UUID channelId) {
        data.computeIfAbsent(userId, k -> new HashSet<>()).add(channelId);
    }

    /**
     * 주어진 유저Id의 참여 채널 목록에 채널ID를 삭제하는 메서드
     *
     * @param userId 유저 ID
     * @param channelId 삭제할 채널 ID
     */
    @Override
    public void delete(UUID userId, UUID channelId) {
        Set<UUID> channelList = data.get(userId);

        if (channelList != null) {
            channelList.remove(channelId);
            if (channelList.isEmpty()) {
                data.remove(userId);
            }
        }

    }

    /**
     * 메모리에 저장된 데이터를 리턴하는 메서드
     *
     * @return 저장된 데이터
     */
    @Override
    public Map<UUID, Set<UUID>> findAll() {
        return data;
    }

    /**
     * 주어진 유저ID에 해당하는 채팅데이터를 조회하는 메서드
     *
     * @param UserId 조회할 유저ID
     * @return 조회된 데이터
     */
    @Override
    public Optional<Set<UUID>> findByUserId(UUID UserId) {
        return Optional.ofNullable(data.get(UserId));
    }
}
