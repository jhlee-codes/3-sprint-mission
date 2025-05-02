package com.sprint.mission.discodeit.dto.Channel;

import com.sprint.mission.discodeit.entity.Channel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelDTO(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String channelName,
        String description,
        boolean isPrivate,
        Instant lastMessageAt,
        List<UUID> userIdList
) {
    public static ChannelDTO from(Channel ch, Instant lastMessageAt, List<UUID> userIdList) {
        return new ChannelDTO(
                ch.getId(),
                ch.getCreatedAt(),
                ch.getUpdatedAt(),
                ch.getChannelName(),
                ch.getDescription(),
                ch.isPrivate(),
                lastMessageAt,
                ch.isPrivate() ? userIdList : null
        );
    }

    /* public 채널 */
    public static ChannelDTO from(Channel ch, Instant lastMessageAt) {
        return from(ch,lastMessageAt,null);
    }
}
