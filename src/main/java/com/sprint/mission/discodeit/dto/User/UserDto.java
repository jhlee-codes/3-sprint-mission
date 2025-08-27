package com.sprint.mission.discodeit.dto.User;

import com.sprint.mission.discodeit.dto.BinaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.Role;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record UserDto(
    UUID id,
    String username,
    String email,
    BinaryContentDto profile,
    boolean online,
    Role role
) {

}
