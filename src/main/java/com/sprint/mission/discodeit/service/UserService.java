package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;

import java.util.Map;
import java.util.UUID;

public interface UserService {
    // 생성
    public User createUser(String userName, String loginId);
    // 전체 조회
    public Map<UUID, User> getUsers();
    // 조회(ID)
    public User getUserById(UUID userId);
    // 조회(유저ID)
    public User getUserByLoginId(String loginId);
    // 수정
    public User updateUser(UUID userId, String userName);
    // 삭제
    public User deleteUser(UUID userId);

}
