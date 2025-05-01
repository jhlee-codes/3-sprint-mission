package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.ChatService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileChatService implements ChatService {
    private static final Path FILE_PATH = Paths.get("data/chat.ser");

    private final Map<UUID, Set<UUID>> data = getUserChannelMap();

    private final MessageService messageService;
    private final ChannelService channelService;
    private final UserService userService;

    public FileChatService(MessageService messageService, ChannelService channelService, UserService userService) {
        this.messageService = messageService;
        this.channelService = channelService;
        this.userService = userService;
    }

    /**
     * 파일에서 읽어온 채팅 데이터를 역직렬화하여 로드하는 메서드
     *
     * @return 파일에 저장된 채팅 데이터
     * @throws RuntimeException 파일 역직렬화 중 예외가 발생한 경우
     */
    public Map<UUID, Set<UUID>> getUserChannelMap() {
        if (!Files.exists(FILE_PATH)) {
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH.toFile()))
        ) {
            return (Map<UUID, Set<UUID>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
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
        // 유저가 이미 해당 채널에 입장해있는 경우
        if (data.containsKey(userId) && data.get(userId).contains(channelId)) {
            throw new IllegalStateException("이미 해당 채널에 입장해있습니다.");
        }

        // 유저의 채널목록 가져와서 추가
        data.computeIfAbsent(userId, k -> new HashSet<>()).add(channelId);
        saveUserChannelMap();
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
        saveUserChannelMap();
    }

    /**
     * 채팅 데이터를 직렬화하여 파일에 저장하는 메서드
     *
     * @throws RuntimeException 파일 생성/직렬화 중 예외가 발생한 경우
     */
    public void saveUserChannelMap() {
        try{
            Files.createDirectories(FILE_PATH.getParent());
            try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH.toFile()))){
                oos.writeObject(data);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
