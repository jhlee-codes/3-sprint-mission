package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.Objects;

public class User extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userName;        // 유저 이름
    private String loginId;          // 유저 ID (검색용 유니크값)
    private boolean isActive;   // 활성여부 (탈퇴시 false)

    public User() {
    }

    public User(String userName, String loginId) {
        this.userName = userName;
        this.loginId = loginId;
        this.isActive = true;
    }

    public String getUserName() {
        return userName;
    }

    public void updateUserName(String userName) {
        this.userName = userName;
        this.updateTimestamp();
    }

    public String getLoginId() {
        return loginId;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void updateIsActive() {
        this.isActive = false;
        this.updateTimestamp();
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", loginId='" + loginId + '\'' +
                (isActive ? "" : "(탈퇴)") +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
