package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    // 생성
    public User createUser(String userName, String userId);
    // 읽기
    public User getUser(UUID id);
    // 모두 읽기
    public List<User> getUsers();
    // 수정
    public User updateUser(User user, String userName);
    // 삭제
    public User deleteUser(UUID id);

    // 유저 ID로 검색
    public User searchUserByUserId(String userId);
}
