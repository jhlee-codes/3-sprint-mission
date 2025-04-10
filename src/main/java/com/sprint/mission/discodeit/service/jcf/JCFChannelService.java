package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.*;

public class JCFChannelService implements ChannelService {
    // 데이터 타입 변경 (List-> Map)
    private final Map<UUID, Channel> data = new HashMap<>();

    @Override
    public Channel createChannel(String channelName) {
        // 중복 이름인 채널 생성 불가
        for (Channel channel : data.values()) {
            if (channel.getChannelName().equals(channelName)) {
                throw new IllegalArgumentException("이미 존재하는 채널입니다. 다른 채널명을 입력해주세요.");
            }
        }
        // 채널 생성 및 컬렉션에 추가
        Channel ch = new Channel(channelName);
        data.put(ch.getId(), ch);
        return ch;
    }

    @Override
    public Channel getChannel(UUID id) {
        return data.values().stream()
                .filter(ch -> ch.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("해당 ID의 채널이 존재하지 않습니다."));
    }

    @Override
    public List<Channel> getChannels() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Channel updateChannel(Channel channel, String channelName) {
        // 채널 유효성 체크
        if (channel == null || !data.containsValue(channel)) {
            throw new NoSuchElementException("존재하지 않는 채널이므로 수정이 불가합니다.");
        }
        // 채널 수정
        for (Channel ch : data.values()) {
            if (ch.getId().equals(channel.getId())) {
                ch.updateChannelName(channelName);
                return ch;
            }
        }
        return null;
    }

    @Override
    public Channel deleteChannel(UUID id) {
        Channel targetChannel = getChannel(id);
        // 채널 유효성 체크
        if (targetChannel == null || !data.containsValue(targetChannel)) {
            throw new NoSuchElementException("존재하지 않는 채널이므로 삭제가 불가합니다.");
        }
        // 채널 컬렉션에서 삭제
        data.remove(id);
        // 유저의 채널리스트에서 채널 삭제
        for (User user : targetChannel.getJoinUserList()) {
            user.deleteJoinChannelList(targetChannel);
        }
        return targetChannel;
    }

    @Override
    public Channel enterChannel(User user, Channel joinChannel) {
        // 채널/유저 유효성 체크
        if (joinChannel == null || !data.containsValue(joinChannel)) {
            throw new NoSuchElementException("존재하지 않는 채널이므로 입장이 불가합니다.");
        } else if (user == null) {
            throw new NoSuchElementException("존재하지 않는 유저이므로 입장이 불가합니다.");
        }
        //유저의 참여중인 채널리스트에 채널 추가
        user.updateJoinChannelList(joinChannel);
        //채널의 유저리스트에 유저 추가
        joinChannel.updateJoinUserList(user);
        return joinChannel;
    }

    @Override
    public Channel leaveChannel(User user, Channel joinChannel) {
        // 채널/유저 유효성 체크
        if (joinChannel == null || !data.containsValue(joinChannel)) {
            throw new NoSuchElementException("존재하지 않는 채널이므로 퇴장이 불가합니다.");
        } else if (user == null) {
            throw new NoSuchElementException("존재하지 않는 유저이므로 퇴장이 불가합니다.");
        } else if (!joinChannel.getJoinUserList().contains(user)) {
            throw new IllegalStateException("채널에 해당 유저가 없으므로 퇴장이 불가합니다.");
        } else if (!user.getIsActive()) {
            throw new IllegalArgumentException("탈퇴한 회원이므로 퇴장이 불가합니다.");
        }
        // 유저의 참여중인 채널리스트에서 채널 제거
        user.deleteJoinChannelList(joinChannel);
        // 채널의 유저리스트에서 유저 제거
        joinChannel.deleteJoinUserList(user);
        return joinChannel;
    }

    @Override
    public Channel searchChannelByChannelName(String channelName) {
        // data를 순회하며 채널명으로 검색
        for (Channel ch : data.values()) {
            if (ch.getChannelName().equals(channelName)) {
                return ch;
            }
        }
        throw new NoSuchElementException("해당 채널을 찾을 수 없습니다.");
    }
}