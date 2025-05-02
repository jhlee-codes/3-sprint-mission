package com.sprint.mission.discodeit.dto.User;

public record UserCreateRequestDTO(
        String userName,
        String email,
        String password
) {}

