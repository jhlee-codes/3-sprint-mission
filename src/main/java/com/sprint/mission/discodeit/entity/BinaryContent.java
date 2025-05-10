package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@ToString
public class BinaryContent implements Serializable {
    private static final long serialVersionUID = -6905250809722555385L;

    /* 공통 필드 */
    private UUID id;
    private Instant createdAt;

    private byte[] content;     // byte 형태의 데이터

    public BinaryContent(byte[] content) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();

        this.content = content;
    }
}
