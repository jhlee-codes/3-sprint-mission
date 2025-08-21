package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.dto.JwtDto;
import com.sprint.mission.discodeit.dto.User.UserDto;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    UserDto getCurrentUserInfo(DiscodeitUserDetails userDetails);

    JwtDto refreshToken(String refreshToken, HttpServletResponse response);
}
