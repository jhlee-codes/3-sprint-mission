package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
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
public class FileReadStatusRepository implements ReadStatusRepository {
    private final Path DIRECTORY;
    private final String EXTENSION = ".ser";

    /**
     * 파일 저장 디렉토리를 설정하고, 해당 디렉토리가 없는 경우 생성
     *
     * @param directory 루트 디렉토리
     * @throws RuntimeException 디렉토리 생성 중 예외가 발생한 경우
     */
    public FileReadStatusRepository(@Value("${discodeit.repository.file-directory}") String directory) {
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), directory, ReadStatus.class.getSimpleName());
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
     * @param id ReadStatus UUID
     * @return 해당 ReadStatus의 저장 경로
     */
    private Path resolvePath(UUID id) {
        return DIRECTORY.resolve(id + EXTENSION);
    }

    /**
     * 주어진 ReadStatus 데이터를 직렬화하여 파일에 저장
     *
     * @param readStatus 저장할 ReadStatus
     * @return 저장한 ReadStatus
     * @throws RuntimeException 파일 직렬화 중 예외가 발생한 경우
     */
    @Override
    public ReadStatus save(ReadStatus readStatus) {
        Path path = resolvePath(readStatus.getId());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()));) {
            oos.writeObject(readStatus);
        } catch (IOException e) {
            throw new RuntimeException("ReadStatus 데이터 파일을 저장하는 중 오류가 발생하였습니다.");
        }
        return readStatus;
    }

    /**
     * 파일에서 읽어온 ReadStatus 데이터를 역직렬화하여 로드
     *
     * @return 읽어온 ReadStatus 데이터
     * @throws RuntimeException 파일 역직렬화 중 예외가 발생한 경우
     */
    @Override
    public List<ReadStatus> findAll() {
        try {
            return Files.list(DIRECTORY)
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()));) {
                            return (ReadStatus) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException("ReadStatus 데이터 파일을 읽는 중 오류가 발생하였습니다.");
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("ReadStatus 데이터 파일을 읽는 중 오류가 발생하였습니다.");
        }
    }

    /**
     * 주어진 id에 해당하는 ReadStatus 조회
     *
     * @param id 조회할 ReadStatus의 id
     * @return 조회된 ReadStatus
     * @throws RuntimeException 파일 역직렬화 중 예외가 발생한 경우
     */
    @Override
    public Optional<ReadStatus> findById(UUID id) {
        ReadStatus rs = null;
        Path path = resolvePath(id);
        if (Files.exists(path)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()));) {
                rs = (ReadStatus) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("ReadStatus 데이터 파일을 읽는 중 오류가 발생하였습니다.");
            }
        }
        return Optional.ofNullable(rs);
    }

    /**
     * 주어진 채널ID에 해당하는 ReadStatus 리스트 조회
     *
     * @param channelId 조회할 ReadStatus의 채널ID
     * @return 조회된 ReadStatus 리스트
     */
    @Override
    public List<ReadStatus> findByChannelId(UUID channelId) {
        return findAll().stream()
                .filter(rs -> rs.getChannelId().equals(channelId))
                .toList();
    }

    /**
     * 주어진 userId에 해당하는 ReadStatus 리스트 조회
     *
     * @param userId 조회할 ReadStatus의 유저ID
     * @return 조회된 ReadStatus
     */
    @Override
    public List<ReadStatus> findByUserId(UUID userId) {
        return findAll().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .toList();
    }

    /**
     * 주어진 channelId에 해당하는 ReadStatus의 유저ID 리스트 조회
     *
     * @param channelId 조회할 channelId
     * @return 조회된 유저ID 리스트
     */
    @Override
    public List<UUID> findUserIdByChannelId(UUID channelId) {
        return findAll().stream()
                .filter(rs -> rs.getChannelId().equals(channelId))
                .map(rs->rs.getUserId())
                .toList();
    }

    /**
     * 주어진 id에 해당하는 ReadStatus 존재여부 판단
     *
     * @param id ReadStatus ID
     * @return 해당 ReadStatus 존재여부
     */
    @Override
    public boolean existsById(UUID id) {
        Path path = resolvePath(id);
        return Files.exists(path);
    }

    /**
     * 주어진 채널ID, 유저ID에 해당하는 ReadStatus 존재여부 판단
     *
     * @param channelId 채널ID
     * @param userId 유저ID
     * @return 해당 ReadStatus 존재여부
     * @throws RuntimeException 파일 역직렬화 중 예외가 발생한 경우
     */
    @Override
    public boolean existsByChannelIdAndUserId(UUID channelId, UUID userId) {
        try {
            return Files.list(DIRECTORY)
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .anyMatch(path -> {
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
                            ReadStatus readStatus = (ReadStatus) ois.readObject();
                            return readStatus.getChannelId().equals(channelId) && readStatus.getUserId().equals(userId);
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException("유저 데이터 파일을 읽는 중 오류가 발생하였습니다.");
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("유저 데이터 파일을 읽는 중 오류가 발생하였습니다.");
        }
    }

    /**
     * 주어진 id에 해당하는 ReadStatus 삭제
     *
     * @param id 삭제할 대상 ReadStatus ID
     * @throws RuntimeException 데이터 삭제중 예외가 발생한 경우
     */
    @Override
    public void deleteById(UUID id) {
        Path path = resolvePath(id);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException("ReadStatus 데이터 삭제 중 오류가 발생하였습니다.");
        }
    }
}
