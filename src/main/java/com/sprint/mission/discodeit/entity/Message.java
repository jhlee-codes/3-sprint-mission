package com.sprint.mission.discodeit.entity;

import java.text.SimpleDateFormat;

public class Message extends BaseEntity {
    private String msgContent;  // 메시지 내용
    private User sendUser;      // 작성자
    private Channel sendChannel;    // 작성 채널
    private boolean isUpdated;  // 메시지 수정 여부

    public Message(Channel sendChannel, User sendUser, String msgContent) {
        this.msgContent = msgContent;
        this.sendUser = sendUser;
        this.sendChannel = sendChannel;
        this.isUpdated = false;
    }

    public Channel getSendChannel() {
        return sendChannel;
    }

    public User getSendUser() {
        return sendUser;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public boolean getIsUpdated() {
        return isUpdated;
    }

    public void updateMsgContent(String msgContent) {
        this.msgContent = msgContent;
        this.isUpdated = true;
        this.setUpdatedAt();
    }

    @Override
    public String toString() {
        return "Message{" +
                "msgId='" + getId() + '\'' +
                ", msgContent='" + msgContent + '\'' + (isUpdated ? "(수정됨)" : "") +
                ", sendUser=" + sendUser.getUserName() + "("+ sendUser.getUserId() +")"+ (sendUser.getIsActive() ? "" : "(탈퇴)") +
                ", sendChannel=" + (sendChannel != null ? sendChannel.getChannelName() : "null") +
                ", createdAt=" + new SimpleDateFormat("yy/MM/dd HH:mm:ss.SSSS").format(getCreatedAt()) +
                ", updatedAt=" + new SimpleDateFormat("yy/MM/dd HH:mm:ss.SSSS").format(getUpdatedAt()) +
                "}\n";
    }
}
