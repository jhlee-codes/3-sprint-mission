package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.Objects;

public class Channel extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String channelName;     // 채널 이름

    public Channel() {
    }

    public Channel(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }

    public void updateChannelName(String channelName) {
        this.channelName = channelName;
        this.updateTimestamp();
    }

    @Override
    public String toString() {
        return "Channel{" +
                "channelName='" + channelName + '\'' +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Channel channel = (Channel) o;
        return Objects.equals(this.getId(), channel.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
