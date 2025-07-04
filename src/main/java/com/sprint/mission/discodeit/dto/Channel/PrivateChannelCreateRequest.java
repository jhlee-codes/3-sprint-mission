package com.sprint.mission.discodeit.dto.Channel;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record PrivateChannelCreateRequest(

        @NotEmpty
        List<UUID> participantIds
) {

}
