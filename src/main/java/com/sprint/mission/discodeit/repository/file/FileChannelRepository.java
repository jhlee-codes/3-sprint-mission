package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
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


@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileChannelRepository implements ChannelRepository {

    private final Path DIRECTORY;
    private final String EXTENSION = ".ser";

    /**
     * 파일 저장 디렉토리를 설정하고, 해당 디렉토리가 없는 경우 생성
     *
     * @param directory 루트 디렉토리
     * @throws RuntimeException 디렉토리 생성 중 예외가 발생한 경우
     */
    public FileChannelRepository(@Value("${discodeit.repository.file-directory}") String directory) {
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), directory, Channel.class.getSimpleName());
        if (Files.notExists(DIRECTORY)) {
            try {
                Files.createDirectories(DIRECTORY);
            } catch (IOException e) {
                throw new RuntimeException("채널 저장 디렉토리를 생성하는 중 오류가 발생했습니다.");
            }
        }
    }

    /**
     * 주어진 ID에 해당하는 파일 경로 생성
     *
     * @param id 채널 ID
     * @return 해당 채널의 저장 경로
     */
    private Path resolvePath(UUID id) {
        return DIRECTORY.resolve(id + EXTENSION);
    }

    /**
     * 주어진 채널을 직렬화하여 파일에 저장
     *
     * @param channel 저장할 채널
     * @return 저장된 채널
     * @throws RuntimeException 파일 직렬화 중 예외가 발생한 경우
     */
    @Override
    public Channel save(Channel channel) {
        Path path = resolvePath(channel.getId());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()));) {
            oos.writeObject(channel);
        } catch (IOException e) {
            throw new RuntimeException("채널 데이터 파일을 저장하는 중 오류가 발생하였습니다.");
        }
        return channel;
    }

    /**
     * 저장된 모든 채널 데이터를 역직렬화하여 로드
     *
     * @return 저장된 채널 데이터 리스트
     * @throws RuntimeException 파일 역직렬화 중 예외가 발생한 경우
     */
    @SuppressWarnings("unchecked")
    public List<Channel> findAll() {
        try {
            return Files.list(DIRECTORY)
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()));) {
                            return (Channel) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException("채널 데이터 파일을 읽는 중 오류가 발생하였습니다.");
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("채널 데이터 파일을 읽는 중 오류가 발생하였습니다.");
        }
    }

    /**
     * 주어진 ID에 해당하는 채널 조회
     *
     * @param id 조회할 채널 ID
     * @return 조회된 채널
     * @throws RuntimeException 파일 역직렬화 중 예외가 발생한 경우
     */
    @Override
    public Optional<Channel> findById(UUID id) {
        Path path = resolvePath(id);
        if (Files.exists(path)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()));) {
                return Optional.of((Channel) ois.readObject());
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("채널 데이터 파일을 읽는 중 오류가 발생하였습니다.");
            }
        }
        return Optional.empty();
    }

    /**
     * 주어진 ID에 해당하는 채널의 존재여부 판단
     *
     * @param id 확인할 채널 ID
     * @return 해당 채널 존재여부
     */
    @Override
    public boolean existsById(UUID id) {
        return Files.exists(resolvePath(id));
    }

    /**
     * 주어진 id에 해당하는 채널 삭제
     *
     * @param id 삭제 대상 채널 id
     * @throws RuntimeException 데이터 삭제 중 예외가 발생한 경우
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
