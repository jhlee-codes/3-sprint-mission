package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf")
public class JCFBinaryContentRepository implements BinaryContentRepository{
    private final Map<UUID, BinaryContent> data;

    public JCFBinaryContentRepository() {
        this.data = new HashMap<>();
    }

    /**
     * 주어진 BinaryContent를 메모리에 저장
     *
     * @param binaryContent 저장할 BinaryContent
     * @return 저장된 BinaryContent
     */
    @Override
    public BinaryContent save(BinaryContent binaryContent) {
        this.data.put(binaryContent.getId(), binaryContent);
        return binaryContent;
    }

    /**
     * 메모리에 저장되어있는 BinaryContent 데이터를 리턴
     *
     * @return 메모리에 저장된 BinaryContent 데이터
     */
    @Override
    public List<BinaryContent> findAll() {
        return this.data.values().stream().toList();
    }

    /**
     * 주어진 id에 해당하는 BinaryContent 조회
     *
     * @param id 조회할 BinaryContent의 ID
     * @return 조회된 BinaryContent
     */
    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return Optional.ofNullable(this.data.get(id));
    }

    /**
     * 주어진 id에 해당하는 BinaryContent의 존재여부 판단
     *
     * @param id BinaryContent id
     * @return 해당 BinaryContent 존재여부
     */
    @Override
    public boolean existsById(UUID id) {
        return this.data.containsKey(id);
    }

    /**
     * 주어진 id에 해당하는 BinaryContent 삭제
     *
     * @param id 삭제할 대상 BinaryContent id
     */
    @Override
    public void deleteById(UUID id) {
        this.data.remove(id);
    }
}
