package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@ToString
public class Channel implements Serializable {
    private static final long serialVersionUID = 79529852066494114L;

    /* 공통 필드 */
    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;

    private String channelName;     // 채널 이름
    private String description;     // 채널 설명
    private boolean isPrivate;      // 비공개채널 여부

    public Channel(boolean isPrivate) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();

        this.isPrivate = isPrivate;
    }

    public Channel(String channelName, String description, boolean isPrivate) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();

        this.channelName = channelName;
        this.description = description;
        this.isPrivate = isPrivate;
    }

    public void update(String newChannelName, String newDescription) {
        boolean isUpdated = false;

        if (newChannelName != null && !newChannelName.equals(this.channelName)) {
            this.channelName = newChannelName;
            isUpdated = true;
        }
        if (newDescription != null && !newDescription.equals(this.description)) {
            this.description = newDescription;
            isUpdated = true;
        }
        if (isUpdated) {
            this.updatedAt = Instant.now();
        }
    }
}
