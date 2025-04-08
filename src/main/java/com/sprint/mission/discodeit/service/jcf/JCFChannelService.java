package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.List;
import java.util.UUID;

public class JCFChannelService implements ChannelService {
    private final List<Channel> data;

    public JCFChannelService(List<Channel> data) {
        this.data = data;
    }

    @Override
    public Channel createChannel(String channelName) {
        // 중복 이름인 채널 생성 불가
        for (Channel channel : data) {
            if (channel.getChannelName().equals(channelName)) {
                System.out.println("이미 존재하는 채널입니다. 다른 채널명을 입력해주세요.");
                return null;
            }
        }

        // 채널 생성
        Channel ch = new Channel(channelName);

        // 채널 컬렉션에 추가
        data.add(ch);
        System.out.println("채널 생성 ) " + channelName + " 생성되었습니다.");
        return ch;
    }

    @Override
    public Channel getChannel(UUID id) {
        return data.stream().filter(ch -> ch.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<Channel> getChannels() {
        return data;
    }

    @Override
    public void updateChannel(Channel channel, String channelName) {
        // 채널 유효성 체크
        if (channel == null || !data.contains(channel)) {
            System.out.println("존재하지 않는 채널이므로 수정이 불가합니다.");
            return;
        }

        String beforeChannelName = channel.getChannelName();

        // 채널 수정
        for (Channel ch : data) {
            if (ch.getId().equals(channel.getId())) {
                ch.updateChannelName(channelName);
                break;
            }
        }
        System.out.println("채널 수정 ) " + beforeChannelName + " -> " + channelName + " 수정되었습니다.");
    }

    @Override
    public void deleteChannel(UUID id) {
        Channel targetChannel = getChannel(id);

        // 채널 유효성 체크
        if (targetChannel == null) {
            System.out.println("삭제할 채널을 찾을 수 없습니다.");
            return;
        }

        String targetChannelName = targetChannel.getChannelName();

        // 채널 컬렉션에서 삭제
        data.remove(targetChannel);

        // 유저의 채널리스트에서 채널 삭제
        for (User user : targetChannel.getJoinUserList()) {
            user.deleteJoinChannelList(targetChannel);
        }
        System.out.println("채널 삭제 ) " + targetChannelName + " 삭제되었습니다.");
    }

    @Override
    public void enterChannel(User user, Channel joinChannel) {
        // 채널/유저 유효성 체크
        if (joinChannel == null || !data.contains(joinChannel)) {
            System.out.println("존재하지 않는 채널이므로 입장이 불가합니다." );
            return;
        } else if (user == null ) {
            System.out.println("존재하지 않는 유저이므로 입장이 불가합니다." );
            return;
        }

        //유저의 참여중인 채널리스트에 채널 추가
        user.updateJoinChannelList(joinChannel);

        //채널의 유저리스트에 유저 추가
        joinChannel.updateJoinUserList(user);
        System.out.println("유저 입장 ) " + user.getUserName() + " 님 " + joinChannel.getChannelName() + " 에 입장하였습니다.");
    }

    @Override
    public Channel leaveChannel(User user, Channel joinChannel) {
        // 채널/유저 유효성 체크
        if (joinChannel == null || !data.contains(joinChannel)) { // 채널
            System.out.println("존재하지 않는 채널이므로 퇴장이 불가합니다.");
            return null;
        } else if (user == null) {
            System.out.println("존재하지 않는 유저이므로 퇴장이 불가합니다." );
            return null;
        } else if (!joinChannel.getJoinUserList().contains(user)) {
            System.out.println("채널에 해당 유저가 없으므로 퇴장이 불가합니다.");
            return null;
        } else if (!user.getIsActive()) {
            System.out.println("탈퇴한 회원이므로 퇴장이 불가합니다.");
            return null;
        }

        // 유저의 참여중인 채널리스트에서 채널 제거
        user.deleteJoinChannelList(joinChannel);

        // 채널의 유저리스트에서 유저 제거
        joinChannel.deleteJoinUserList(user);
        System.out.println("유저 퇴장 ) " + user.getUserName() + " 가 " + joinChannel.getChannelName() + " 에서 퇴장하였습니다.");
        return joinChannel;
    }

    @Override
    public Channel searchChannelByChannelName(String channelName) {
        // data를 순회하며 채널명으로 검색
        for (Channel ch : data) {
            if (ch.getChannelName().equals(channelName)) {
                return ch;
            }
        }
        return null;
    }
}