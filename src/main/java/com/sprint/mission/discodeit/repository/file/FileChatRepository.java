package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.repository.ChatRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileChatRepository implements ChatRepository {
    private static final Path FILE_PATH = Paths.get("data/chat.ser");
    private final Map<UUID, Set<UUID>> data;

    public FileChatRepository() {
        this.data = findAll();
    }

    public FileChatRepository(Map<UUID, Set<UUID>> data) {
        this.data = data;
    }

    /**
     * 채팅 데이터를 직렬화하여 파일에 저장하는 메서드
     *
     * @throws RuntimeException 파일 생성/직렬화 중 예외가 발생한 경우
     */
    public void saveAll() {
        try{
            Files.createDirectories(FILE_PATH.getParent());
            try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH.toFile()))){
                oos.writeObject(data);
            }
        } catch (IOException e) {
            throw new RuntimeException("채팅 데이터 파일을 저장하는 중 오류가 발생하였습니다.");
        }
    }

    /**
     * 주어진 유저ID의 참여 채널 목록에 채널ID 추가
     *
     * @param userId 참여 유저 ID
     * @param channelId 참여 채널 ID
     */
    @Override
    public void save(UUID userId, UUID channelId) {
        data.computeIfAbsent(userId, k -> new HashSet<>()).add(channelId);
        saveAll();
    }

    /**
     * 주어진 유저Id의 참여 채널 목록에 채널ID를 삭제하는 메서드
     *
     * @param userId 퇴장 유저 ID
     * @param channelId 퇴장 채널 ID
     */
    @Override
    public void delete(UUID userId, UUID channelId) {
        Set<UUID> channelList = data.get(userId);

        if (channelList != null) {
            channelList.remove(channelId);
            if (channelList.isEmpty()) {
                data.remove(userId);
            }
        }
        saveAll();
    }

    /**
     * 파일에서 읽어온 채팅 데이터를 역직렬화하여 로드하는 메서드
     *
     * @return 저장된 채팅 데이터
     * @throws RuntimeException 파일 역직렬화 중 예외가 발생한 경우
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<UUID, Set<UUID>> findAll() {
        if (!Files.exists(FILE_PATH)) {
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH.toFile()))) {
            return (Map<UUID, Set<UUID>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("채팅 데이터 파일을 읽는 중 오류가 발생하였습니다.");
        }    }

    /**
     * 주어진 유저ID에 해당하는 채팅데이터를 조회하는 메서드
     *
     * @param UserId 조회할 유저ID
     * @return 조회된 데이터
     */
    @Override
    public Optional<Set<UUID>> findByUserId(UUID UserId) {
        return Optional.ofNullable(data.get(UserId));
    }
}
