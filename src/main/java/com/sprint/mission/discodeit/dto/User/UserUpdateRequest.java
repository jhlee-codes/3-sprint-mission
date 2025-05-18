package com.sprint.mission.discodeit.dto.User;

public record UserUpdateRequestDTO (
        String newUsername,
        String newEmail,
        String newPassword
){}
