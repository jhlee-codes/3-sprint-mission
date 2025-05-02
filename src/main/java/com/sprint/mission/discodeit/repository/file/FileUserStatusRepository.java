package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
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
public class FileUserStatusRepository implements UserStatusRepository {
    private final Path DIRECTORY;
    private final String EXTENSION = ".ser";

    /**
     * 파일 저장 디렉토리를 설정하고, 해당 디렉토리가 없는 경우 생성
     *
     * @param directory 루트 디렉토리
     * @throws RuntimeException 디렉토리 생성 중 예외가 발생한 경우
     */
    public FileUserStatusRepository(@Value("${discodeit.repository.file-directory}") String directory) {
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), directory, UserStatus.class.getSimpleName());
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
     * @param id UserStatus UUID
     * @return 해당 UserStatus의 저장 경로
     */
    private Path resolvePath(UUID id) {
        return DIRECTORY.resolve(id + EXTENSION);
    }

    /**
     * 주어진 UserStatus 데이터를 직렬화하여 파일에 저장
     *
     * @param userStatus 저장할 UserStatus
     * @return 저장한 UserStatus
     * @throws RuntimeException 파일 직렬화 중 예외가 발생한 경우
     */
    @Override
    public UserStatus save(UserStatus userStatus) {
        Path path = resolvePath(userStatus.getId());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()));) {
            oos.writeObject(userStatus);
        } catch (IOException e) {
            throw new RuntimeException("UserStatus 데이터 파일을 저장하는 중 오류가 발생하였습니다.");
        }
        return userStatus;
    }

    /**
     * 파일에서 읽어온 UserStatus 데이터를 역직렬화하여 로드
     *
     * @return 읽어온 UserStatus 데이터 리스트
     * @throws RuntimeException 파일 역직렬화 중 예외가 발생한 경우
     */
    @Override
    public List<UserStatus> findAll() {
        try {
            return Files.list(DIRECTORY)
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()));) {
                            return (UserStatus) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException("UserStatus 데이터 파일을 읽는 중 오류가 발생하였습니다.");
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("UserStatus 데이터 파일을 읽는 중 오류가 발생하였습니다.");
        }
    }

    /**
     * 주어진 id에 해당하는 UserStatus 조회
     *
     * @param id 조회할 UserStatus의 id
     * @return 조회된 UserStatus
     * @throws RuntimeException 파일 역직렬화 중 예외가 발생한 경우
     */
    @Override
    public Optional<UserStatus> findById(UUID id) {
        UserStatus us = null;
        Path path = resolvePath(id);
        if (Files.exists(path)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()));) {
                us = (UserStatus) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("UserStatus 데이터 파일을 읽는 중 오류가 발생하였습니다.");
            }
        }
        return Optional.ofNullable(us);
    }

    /**
     * 주어진 유저ID에 해당하는 UserStatus 조회
     *
     * @param userId 조회할 UserStatus의 유저ID
     * @return 조회된 UserStatus
     */
    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return findAll().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .findFirst();
    }

    /**
     * 주어진 id에 해당하는 UserStatus 존재여부 판단
     *
     * @param id UserStatus id
     * @return 해당 UserStatus 존재여부
     */
    @Override
    public boolean existsById(UUID id) {
        Path path = resolvePath(id);
        return Files.exists(path);
    }

    /**
     * 주어진 id에 해당하는 UserStatus 삭제
     *
     * @param id 삭제할 대상 UserStatus ID
     * @throws RuntimeException 데이터 삭제중 예외가 발생한 경우
     */
    @Override
    public void deleteById(UUID id) {
        Path path = resolvePath(id);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException("UserStatus 데이터 삭제 중 오류가 발생하였습니다.");
        }
    }
}
