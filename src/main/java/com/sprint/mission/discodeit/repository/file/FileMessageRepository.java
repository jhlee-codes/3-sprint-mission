package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileMessageRepository implements MessageRepository {
    private final Path DIRECTORY;
    private final String EXTENSION = ".ser";

    /**
     * 파일 저장 디렉토리를 설정하고, 해당 디렉토리가 없는 경우 생성
     *
     * @param directory 루트 디렉토리
     * @throws RuntimeException 디렉토리 생성 중 예외가 발생한 경우
     */
    public FileMessageRepository(@Value("${discodeit.repository.file-directory}") String directory) {
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), directory, Message.class.getSimpleName());
        if (Files.notExists(DIRECTORY)) {
            try {
                Files.createDirectories(DIRECTORY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 주어진 UUID에 대응하는 파일 경로 생성
     *
     * @param id 메세지 UUID
     * @return 해당 메세지의 저장 경로
     */
    private Path resolvePath(UUID id) {
        return DIRECTORY.resolve(id + EXTENSION);
    }

    /**
     * 주어진 메시지 데이터를 직렬화하여 파일에 저장
     *
     * @param message 저장할 메시지
     * @return 저장한 메시지
     * @throws RuntimeException 파일 생성/직렬화 중 예외가 발생한 경우
     */
    @Override
    public Message save(Message message) {
        Path path = resolvePath(message.getId());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()));) {
            oos.writeObject(message);
        } catch (IOException e) {
            throw new RuntimeException("메시지 데이터 파일을 저장하는 중 오류가 발생하였습니다.");
        }
        return message;
    }

    /**
     * 파일에서 읽어온 메시지 데이터를 역직렬화하여 로드
     *
     * @return 역직렬화된 메시지 데이터
     * @throws RuntimeException 파일 역직렬화 중 예외가 발생한 경우
     */
    public List<Message> findAll() {
        try {
            return Files.list(DIRECTORY)
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()));) {
                            return (Message) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException("메시지 데이터 파일을 읽는 중 오류가 발생하였습니다.");
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("메시지 데이터 파일을 읽는 중 오류가 발생하였습니다.");
        }
    }

    /**
     * 주어진 id에 해당하는 메시지 조회
     *
     * @param id 조회할 메시지의 ID
     * @return 조회된 메시지
     * @throws RuntimeException 파일 역직렬화 중 예외가 발생한 경우
     */
    @Override
    public Optional<Message> findById(UUID id) {
        Message msg = null;
        Path path = resolvePath(id);
        if (Files.exists(path)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()));) {
                msg = (Message) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("메시지 데이터 파일을 읽는 중 오류가 발생하였습니다.");
            }
        }
        return Optional.ofNullable(msg);
    }

    /**
     * 주어진 채널ID에 해당하는 메시지 리스트 조회
     *
     * @param channelId 조회할 메시지의 채널ID
     * @return 조회된 메시지 리스트
     */
    @Override
    public List<Message> findByChannelId(UUID channelId) {
        return findAll().stream()
                .filter(m -> m.getChannelId().equals(channelId))
                .collect(Collectors.toList());
    }

    /**
     * 주어진 id에 해당하는 메시지 존재여부 판단
     *
     * @param id 메시지 id
     * @return 해당 메시지 존재여부
     */
    @Override
    public boolean existsById(UUID id) {
        Path path = resolvePath(id);
        return Files.exists(path);
    }

    /**
     * 주어진 id에 해당하는 메시지 삭제
     *
     * @param id 삭제할 대상 메시지 id
     * @throws RuntimeException 데이터 삭제중 예외가 발생한 경우
     */
    @Override
    public void deleteById(UUID id) {
        Path path = resolvePath(id);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException("채널 데이터 삭제 중 오류가 발생하였습니다.");
        }
    }
}
