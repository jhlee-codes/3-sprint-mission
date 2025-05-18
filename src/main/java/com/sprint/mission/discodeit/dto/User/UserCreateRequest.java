package com.sprint.mission.discodeit.dto.User;

public record UserCreateRequestDTO(
        String username,
        String email,
        String password
) {

}

