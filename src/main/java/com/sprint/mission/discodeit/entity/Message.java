package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Message extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String msgContent;  // 메시지 내용
    private UUID sendUserId;      // 송신자 id
    private UUID channelId;    // 채널 id
    private boolean isUpdated;  // 메시지 수정 여부
    private boolean isDeleted;  // 메시지 삭제 여부

    public Message() {
    }

    public Message(UUID channelId, UUID sendUserId, String msgContent) {
        this.msgContent = msgContent;
        this.sendUserId = sendUserId;
        this.channelId = channelId;
        this.isUpdated = false;
        this.isDeleted = false;
    }


    public UUID getSendUserId() {
        return sendUserId;
    }

    public UUID getChannelId() {
        return channelId;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public boolean getIsUpdated() {
        return isUpdated;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void updateMsgContent(String msgContent) {
        this.msgContent = msgContent;
        this.isUpdated = true;
        this.updateTimestamp();
    }

    public void deleteMsgContent() {
        this.msgContent = "";
        this.isDeleted = true;
    }

    @Override
    public String toString() {
        return "Message{" +
                "msgContent='" + msgContent + '\'' +
                (isUpdated ? "(수정됨)" : "") +
                (isDeleted ? "--삭제된 메세지입니다.--" : "") +
                ", senderId=" + sendUserId +
                ", channelId=" + channelId +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(getId(), message.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
