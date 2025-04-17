package com.sprint.mission.discodeit.entity;

import org.w3c.dom.ls.LSOutput;

import java.util.UUID;

public class BaseEntity {
    private UUID id;
    private Long createdAt;
    private Long updatedAt;

    public BaseEntity() {
        this.id = UUID.randomUUID();    // UUID 자동생성
        this.createdAt = System.currentTimeMillis();    // 시간(밀리초 기준)
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }
}
