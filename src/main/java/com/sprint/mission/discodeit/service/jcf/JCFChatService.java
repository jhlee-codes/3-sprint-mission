package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.ChatService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFChatService implements ChatService {

    private final Map<UUID, Set<UUID>> data = new HashMap<>();

    private final MessageService messageService;
    private final ChannelService channelService;
    private final UserService userService;

    public JCFChatService (MessageService messageService, ChannelService channelService, UserService userService) {
        this.messageService = messageService;
        this.channelService = channelService;
        this.userService = userService;
    }

    /**
     * 메모리에 저장되어있는 채팅 데이터를 리턴하는 메서드
     *
     * @return 메모리에 저장된 채팅 데이터
     */
    @Override
    public Map<UUID, Set<UUID>> getUserChannelMap() {
        return data;
    }

    /**
     * 유저가 주어진 채널에 입장되도록 처리하는 메서드
     *
     * @param userId 입장할 유저ID
     * @param channelId 입장할 채널ID
     */
    @Override
    public void enterChannel(UUID userId, UUID channelId) {
        // 채널/유저 유효성 체크
        Channel ch = channelService.getChannelById(channelId);
        User targetUser = userService.getUserById(userId);
        // 유저의 채널목록 가져와서 추가
        data.computeIfAbsent(userId, k -> new HashSet<>()).add(channelId);
    }

    /**
     * 유저가 주어진 채널에서 퇴장하도록 처리하는 메서드
     *
     * @param userId 퇴장할 유저ID
     * @param channelId 퇴장할 채널ID
     * @
     */
    @Override
    public void leaveChannel(UUID userId, UUID channelId) {
        // 채널/유저 유효성 체크
        Channel ch = channelService.getChannelById(channelId);
        User joinUser = userService.getUserById(userId);

        Set<UUID> channelList = data.get(userId);

        if (!joinUser.getIsActive()) {
            throw new IllegalArgumentException("탈퇴한 회원이므로 퇴장이 불가합니다.");
        }

        if (channelList != null) {
            channelList.remove(channelId);
            if (channelList.isEmpty()) {
                data.remove(userId);
            }
        }
    }

}
