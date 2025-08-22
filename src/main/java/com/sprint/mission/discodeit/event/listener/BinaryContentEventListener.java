package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentEventListener {

    private final BinaryContentStorage binaryContentStorage;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBinaryContentCreated(BinaryContentCreatedEvent event) {

        UUID binaryContentId = event.binaryContentId();
        byte[] bytes = event.bytes();
        log.debug("[BinaryContentEventListener] 바이너리 데이터 storyge에 저장 시작 - id: {}", binaryContentId);

        binaryContentStorage.put(binaryContentId, bytes);
    }
}
