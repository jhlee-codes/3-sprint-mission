package com.sprint.mission.discodeit.dto.ReadStatus;

import java.time.Instant;

public record ReadStatusUpdateRequestDTO(
        Instant lastReadAt
){}
