package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentEventListener {

    private final BinaryContentStorage binaryContentStorage;
    private final BinaryContentService binaryContentService;

    @Async("fileTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBinaryContentCreated(BinaryContentCreatedEvent event) {

        UUID binaryContentId = event.binaryContentId();
        byte[] bytes = event.bytes();
        log.debug("[BinaryContentEventListener] 바이너리 데이터 storage에 저장 시작 - id: {}", binaryContentId);

        try {
            binaryContentStorage.put(binaryContentId, bytes);
            binaryContentService.updateStatus(binaryContentId, BinaryContentStatus.SUCCESS);
            log.debug("[BinaryContentEventListener] 바이너리 데이터 storage에 저장 성공");
        } catch (Exception e) {
            binaryContentService.updateStatus(binaryContentId, BinaryContentStatus.FAIL);
            log.error("[BinaryContentEventListener] 바이너리 데이터 storage에 저장 중 예외 발생");
        }
    }
}
