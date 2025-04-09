package com.sprint.mission.discodeit.entity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Channel extends BaseEntity {
    private String channelName;     // 채널 이름
    private List<Message> messageList;      // 채널에 등록된 메시지 리스트
    private List<User> joinUserList;        // 채널에 참여중인 유저리스트

    public Channel(String channelName) {
        this.channelName = channelName;
        this.messageList = new ArrayList<>();
        this.joinUserList = new ArrayList<>();
    }

    public String getChannelName() {
        return channelName;
    }

    public void updateChannelName(String channelName) {
        this.channelName = channelName;
        this.updateUpdatedAt();
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void updateMessageList(Message message) {
        this.messageList.add(message);
        this.updateUpdatedAt();
    }

    public void deleteMessageList(Message message) {
        this.messageList.remove(message);
        this.updateUpdatedAt();
    }

    public List<User> getJoinUserList() {
        return joinUserList;
    }

    public void updateJoinUserList(User user) {
        this.joinUserList.add(user);
        this.updateUpdatedAt();
    }

    public void deleteJoinUserList(User user) {
        this.joinUserList.remove(user);
        this.updateUpdatedAt();
    }

    @Override
    public String toString() {
        return "Channel{" +
                "chId='" + getId() + '\'' +
                ", channelName='" + channelName + '\'' +
                ", messageList=" + messageList.stream().map( m -> m.getSendUser().getUserName()+ " : " + m.getMsgContent() + (m.getIsUpdated() ? "(수정됨)" : "")).toList() +
                ", joinUserList=" +  joinUserList.stream().map(u -> u.getUserName() + (u.getIsActive() ? "" : "(탈퇴)")).toList() +
                ", createdAt=" + new SimpleDateFormat("yy/MM/dd HH:mm:ss").format(getCreatedAt())  +
                ", updatedAt=" + new SimpleDateFormat("yy/MM/dd HH:mm:ss").format(getUpdatedAt()) +
                "}\n";
    }
}
