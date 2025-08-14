package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.dto.User.UserDto;

public record JwtInformation(
    UserDto userDto,
    String accessToken,
    String refreshToken
) {

    public JwtInformation rotate(String accessToken, String refreshToken) {
        return new JwtInformation(this.userDto, accessToken, refreshToken);
    }
}
