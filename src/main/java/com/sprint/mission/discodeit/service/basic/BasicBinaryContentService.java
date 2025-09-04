package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.annotation.Logging;
import com.sprint.mission.discodeit.dto.BinaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.Notification.NotificationDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.BinaryContent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.SseService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Logging
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentMapper binaryContentMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final SseService sseService;

    /**
     * 주어진 요청 DTO를 기반으로 BinaryContent 생성
     *
     * @param createRequest 생성 요청 DTO
     * @return 생성된 BinaryContent
     */
    @Override
    @Transactional
    public BinaryContentDto create(BinaryContentCreateRequest createRequest) {

        BinaryContent binaryContent = BinaryContent.builder()
            .fileName(createRequest.fileName())
            .contentType(createRequest.contentType())
            .size(((long) createRequest.bytes().length))
            .build();

        binaryContentRepository.save(binaryContent);
        BinaryContentCreatedEvent event = new BinaryContentCreatedEvent(binaryContent.getId(),
            createRequest.bytes());
        eventPublisher.publishEvent(event);

        return binaryContentMapper.toDto(binaryContent);
    }

    /**
     * 주어진 ID 목록에 해당하는 BinaryContent 전체 조회
     *
     * @param ids 조회할 BinaryContent ID 목록
     * @return 조회된 BinaryContent 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {

        return binaryContentRepository.findAllById(ids).stream()
            .map(binaryContentMapper::toDto)
            .toList();
    }

    /**
     * 주어진 id에 해당하는 BinaryContent 조회
     *
     * @param id 조회할 BinaryContent ID
     * @return 조회된 BinaryContent
     * @throws BinaryContentNotFoundException 존재하는 BinaryContent가 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public BinaryContentDto find(UUID id) {
        BinaryContent binaryContent = binaryContentRepository.findById(id)
            .orElseThrow(() -> new BinaryContentNotFoundException(id));

        return binaryContentMapper.toDto(binaryContent);
    }

    /**
     * 주어진 id에 해당하는 BinaryContent 삭제
     *
     * @param id 삭제할 BinaryContent id
     * @throws BinaryContentNotFoundException 존재하는 BinaryContent가 없는 경우
     */
    @Override
    @Transactional
    public void delete(UUID id) {
        if (!binaryContentRepository.existsById(id)) {
            throw new BinaryContentNotFoundException(id);
        }

        binaryContentRepository.deleteById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BinaryContentDto updateStatus(UUID id, BinaryContentStatus status) {

        BinaryContent binaryContent = binaryContentRepository.findById(id)
            .orElseThrow(() -> new BinaryContentNotFoundException(id));

        binaryContent.updateStatus(status);

        BinaryContentDto binaryContentDto = binaryContentMapper.toDto(binaryContent);

        // SSE Send
        sseService.broadcast("binaryContents.updated",
            binaryContentDto);

        return binaryContentDto;
    }
}
