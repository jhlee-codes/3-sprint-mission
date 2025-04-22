package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChatRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.ChatService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BasicChatService implements ChatService {
    private final ChannelService channelService;
    private final MessageService messageService;
    private final UserService userService;
    private final ChatRepository chatRepository;

    public BasicChatService(ChannelService channelService, MessageService messageService, UserService userService, ChatRepository chatRepository) {
        this.channelService = channelService;
        this.messageService = messageService;
        this.userService = userService;
        this.chatRepository = chatRepository;
    }


    /**
     * 유저가 주어진 채널에 입장되도록 처리하는 메서드
     *
     * @param userId 입장할 유저ID
     * @param channelId 입장할 채널ID
     * @throws IllegalStateException 유저가 이미 해당 채널에 입장해있는 경우
     */
    @Override
    public void enterChannel(UUID userId, UUID channelId) {
        // 채널/유저 유효성 체크
        Channel ch = channelService.getChannelById(channelId);
        User targetUser = userService.getUserById(userId);

        Map<UUID, Set<UUID>> data = chatRepository.findAll();

        // 유저가 이미 해당 채널에 입장해있는 경우
        if (data != null && data.containsKey(userId) && data.get(userId).contains(channelId)) {
            throw new IllegalStateException("이미 해당 채널에 입장해있습니다.");
        }

        // 파일에 저장
        chatRepository.save(userId, channelId);

    }

    /**
     * 유저가 주어진 채널에서 퇴장하도록 처리하는 메서드
     *
     * @param userId 퇴장할 유저ID
     * @param channelId 퇴장할 채널ID
     * @throws IllegalStateException 유저가 해당 채널에 없는 경우
     * @throws IllegalArgumentException 유저가 탈퇴한 상태인 경우
     */
    @Override
    public void leaveChannel(UUID userId, UUID channelId) {
        // 채널/유저 유효성 체크
        Channel ch = channelService.getChannelById(channelId);
        User joinUser = userService.getUserById(userId);

        Map<UUID, Set<UUID>> data = chatRepository.findAll();
        Set<UUID> channelList = data.get(userId);

        if (!joinUser.getIsActive()) {
            throw new IllegalArgumentException("탈퇴한 회원이므로 퇴장이 불가합니다.");
        }

        chatRepository.delete(userId, channelId);

    }

    /**
     * 레포지토리에서 읽어온 채널 데이터를 리턴하는 메서드
     *
     * @return 저장된 채널 데이터
     */
    @Override
    public Map<UUID, Set<UUID>> getUserChannelMap() {
        return chatRepository.findAll();
    }
}
