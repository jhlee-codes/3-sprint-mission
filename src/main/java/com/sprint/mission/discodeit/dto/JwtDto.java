package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.dto.User.UserDto;

public record JwtDto(
    UserDto userDto,
    String accessToken
) {

}
