package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.auth.jwt.store.JwtRegistry;
import com.sprint.mission.discodeit.dto.BinaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.entity.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final SessionRegistry sessionRegistry;
    private final JwtRegistry jwtRegistry;
    private final BinaryContentMapper binaryContentMapper;

    public UserDto toDto(User user) {

        BinaryContentDto profile = user.getProfile() != null
            ? binaryContentMapper.toDto(user.getProfile()) : null;

        boolean online = isUserOnline(user.getId());

        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            profile,
            online,
            user.getRole()
        );
    }

    public boolean isUserOnline(UUID userId) {
        return jwtRegistry.hasActiveJwtInformationByUserId(userId);
    }
}
