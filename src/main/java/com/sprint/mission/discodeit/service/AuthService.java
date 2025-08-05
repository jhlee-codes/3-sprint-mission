package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.User.UserDto;

public interface AuthService {

    UserDto getCurrentUserInfo(DiscodeitUserDetails userDetails);

    boolean isUserOnline(String username);
}
