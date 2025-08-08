package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.dto.BinaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.entity.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final SessionRegistry sessionRegistry;
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
        return sessionRegistry.getAllPrincipals().stream()
            .filter(DiscodeitUserDetails.class::isInstance)
            .map(DiscodeitUserDetails.class::cast)
            .filter(user -> user.getId().equals(userId))
            .anyMatch(user -> !sessionRegistry.getAllSessions(user, false).isEmpty());
    }
}
