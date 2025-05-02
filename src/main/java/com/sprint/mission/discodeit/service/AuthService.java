package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.User.UserLoginRequestDTO;
import com.sprint.mission.discodeit.entity.User;

public interface AuthService {
    // 로그인
    User login(UserLoginRequestDTO loginRequestDTO);
}
