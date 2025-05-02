package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
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
public class FileBinaryContentRepository implements BinaryContentRepository {
    private final Path DIRECTORY;
    private final String EXTENSION = ".ser";

    /**
     * 파일 저장 디렉토리를 설정하고, 해당 디렉토리가 없는 경우 생성
     *
     * @param directory 루트 디렉토리
     * @throws RuntimeException 디렉토리 생성 중 예외가 발생한 경우
     */
    public FileBinaryContentRepository(@Value("${discodeit.repository.file-directory}") String directory) {
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), directory, BinaryContent.class.getSimpleName());
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
     * @param id BinaryContent UUID
     * @return 해당 BinaryContent의 저장 경로
     */
    private Path resolvePath(UUID id) {
        return DIRECTORY.resolve(id + EXTENSION);
    }

    /**
     * 주어진 BinaryContent를 직렬화하여 파일에 저장
     *
     * @param binaryContent 저장할 BinaryContent
     * @return binaryContent 저장한 BinaryContent
     * @throws RuntimeException 파일 생성/직렬화 중 예외가 발생한 경우
     */
    @Override
    public BinaryContent save(BinaryContent binaryContent) {
        Path path = resolvePath(binaryContent.getId());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()));) {
            oos.writeObject(binaryContent);
        } catch (IOException e) {
            throw new RuntimeException("BinaryContent 데이터 파일을 저장하는 중 오류가 발생하였습니다.");
        }
        return binaryContent;
    }

    /**
     * 파일에서 읽어온 BinaryContent 데이터를 역직렬화하여 로드
     *
     * @return 저장된 BinaryContent 데이터 리스트
     * @throws RuntimeException 파일 역직렬화 중 예외가 발생한 경우
     */
    @Override
    public List<BinaryContent> findAll() {
        try {
            return Files.list(DIRECTORY)
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()));) {
                            return (BinaryContent) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException("BinaryContent 데이터 파일을 읽는 중 오류가 발생하였습니다.");
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("BinaryContent 데이터 파일을 읽는 중 오류가 발생하였습니다.");
        }
    }

    /**
     * 주어진 id에 해당하는 BinaryContent를 조회
     *
     * @param id 조회할 BinaryContent의 ID
     * @return 조회된 BinaryContent
     * @throws RuntimeException 파일 역직렬화 중 예외가 발생한 경우
     */
    @Override
    public Optional<BinaryContent> findById(UUID id) {
        BinaryContent bc = null;
        Path path = resolvePath(id);
        if (Files.exists(path)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()));) {
                bc = (BinaryContent) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("BinaryContent 데이터 파일을 읽는 중 오류가 발생하였습니다.");
            }
        }
        return Optional.ofNullable(bc);
    }

    /**
     * 주어진 id에 해당하는 BinaryContent의 존재여부 판단
     *
     * @param id BinaryContent ID
     * @return 존재여부
     */
    @Override
    public boolean existsById(UUID id) {
        Path path = resolvePath(id);
        return Files.exists(path);
    }

    /**
     * 주어진 id에 해당하는 BinaryContent 삭제
     *
     * @param id 삭제할 대상 BinaryContent ID
     * @throws RuntimeException 데이터 삭제중 예외가 발생한 경우
     */
    @Override
    public void deleteById(UUID id) {
        Path path = resolvePath(id);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException("BinaryContent 데이터 삭제 중 오류가 발생하였습니다.");
        }
    }
}
