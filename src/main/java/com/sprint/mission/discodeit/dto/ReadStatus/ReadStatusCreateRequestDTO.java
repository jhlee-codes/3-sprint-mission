package com.sprint.mission.discodeit.dto.ReadStatus;

import java.util.UUID;

public record ReadStatusCreateRequestDTO(
        UUID userId,
        UUID channelId
) {}
