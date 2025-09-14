package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.dto.Jwt.JwtDto;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

public interface AuthService {

    UserDto getCurrentUserInfo(DiscodeitUserDetails userDetails);

    JwtDto refreshToken(String refreshToken, HttpServletResponse response);

    UserDto updateUserRole(UUID userId, Role newRole);
}
