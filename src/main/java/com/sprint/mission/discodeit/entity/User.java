package com.sprint.mission.discodeit.entity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class User extends BaseEntity {
    private String userName;        // 유저 이름
    private String userId;          // 유저 ID (검색용 유니크값)
    private List<Channel> joinChannelList;      // 유저가 참여중인 채널리스트
    private boolean isActive;   // 활성여부 (탈퇴시 false)

    public User(String userName, String userId) {
        this.userName = userName;
        this.userId = userId;
        this.joinChannelList = new ArrayList<>();
        this.isActive = true;
    }

    public String getUserName() {
        return userName;
    }

    public void updateUserName(String userName) {
        this.userName = userName;
        this.updatedUpdatedAt();
    }

    public String getUserId() {
        return userId;
    }

    public List<Channel> getJoinChannelList() {
        return joinChannelList;
    }

    public void updateJoinChannelList(Channel joinChannel) {
        this.joinChannelList.add(joinChannel);
        this.updatedUpdatedAt();
    }

    public void deleteJoinChannelList(Channel joinChannel) {
        this.joinChannelList.remove(joinChannel);
        this.updatedUpdatedAt();
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void updateIsActive() {
        this.isActive = false;
        this.updatedUpdatedAt();
    }


    @Override
    public String toString() {
        return "User{" +
                "Id='" +getId() + '\'' +
                ", userName='" + userName + '\'' +
                ", userId='" + userId + '\'' +
                (isActive ? "" : "(탈퇴)") +
                ", joinChannelList=" + joinChannelList.stream().map(Channel::getChannelName).toList() +
                ", createdAt=" + new SimpleDateFormat("yy/MM/dd HH:mm:ss.SSSS").format(getCreatedAt()) +
                ", updatedAt=" + new SimpleDateFormat("yy/MM/dd HH:mm:ss.SSSS").format(getUpdatedAt()) +
                "}\n";
    }
}
