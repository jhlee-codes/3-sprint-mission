package com.sprint.mission.discodeit.service.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.Sse.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import com.sprint.mission.discodeit.service.SseService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicSseService implements SseService {

    private static final long DEFAULT_TIMEOUT = 30L * 60L * 1000L;

    private final SseEmitterRepository emitterRepository;
    private final SseMessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    @Override
    public SseEmitter connect(UUID receiverId, UUID lastEventId) {
        log.debug("[BasicSseService] SSE 연결 요청: receiverId={}, lastEventId={}", receiverId,
            lastEventId);
        log.debug("[BasicSseService] SseEmitterRepository instance hash: {}",
            emitterRepository.hashCode());

        if (receiverId == null) {
            throw new IllegalArgumentException("receiverId는 필수값입니다.");
        }

        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);

        sseEmitter.onCompletion(() -> {
            log.debug("[BasicSseService] SSE Emitter 완료: receiverId={}", receiverId);
            emitterRepository.remove(receiverId, sseEmitter);
        });
        sseEmitter.onTimeout(() -> {
            log.debug("[BasicSseService] SSE Emitter 타임아웃: receiverId={}", receiverId);
            emitterRepository.remove(receiverId, sseEmitter);
        });
        sseEmitter.onError(t -> {
            log.warn("[SSE] onError rid={} err={}", receiverId,
                t != null ? t.toString() : "null");
            emitterRepository.remove(receiverId, sseEmitter);
        });

        emitterRepository.add(receiverId, sseEmitter);
        log.debug("[BasicSseService] SSE Emitter 등록 완료 - receiverId={}", receiverId);

        ping(sseEmitter);

        if (lastEventId != null) {
            messageRepository.findAfter(lastEventId).stream()
                .filter(msg -> msg.receiverIds() == null || msg.receiverIds()
                    .contains(receiverId))
                .forEach(msg -> {
                    safeSend(receiverId, sseEmitter, msg);
                });
        }

        return sseEmitter;
    }

    @Override
    public void send(Collection<UUID> receiverIds, String eventName, Object data) {

        log.debug("[BasicSseService] 이벤트 전송: receiverIds={} event={} ", receiverIds,
            eventName);

        SseMessage msg = new SseMessage(UUID.randomUUID(), eventName, data,
            new HashSet<>(receiverIds));
        messageRepository.save(msg);

        for (UUID receiverId : receiverIds) {
            for (SseEmitter emitter : emitterRepository.get(receiverId)) {
                safeSend(receiverId, emitter, msg);
            }
        }
    }

    @Override
    public void broadcast(String eventName, Object data) {

        log.debug("[BasicSseService] 이벤트 전체 전송: event={} ", eventName);
        SseMessage msg = new SseMessage(UUID.randomUUID(), eventName, data, null);
        messageRepository.save(msg);

        emitterRepository.findAll()
            .forEach((rid, list) -> list.forEach(emitter -> {
                safeSend(rid, emitter, msg);
            }));
    }

    @Scheduled(fixedRate = 1000 * 60 * 30)
    @Override
    public void cleanUp() {

        log.debug("[BasicSseService] 만료된 SseEmitter 객체 삭제 시작");

        // 동시성 방지를 위해 리스트 복사본 순회
        emitterRepository.findAll().forEach((receiverId, emitters) -> {
            List<SseEmitter> emittersCopy = new ArrayList<>(emitters);
            for (SseEmitter emitter : emittersCopy) {
                if (!ping(emitter)) {
                    emitterRepository.remove(receiverId, emitter);
                    log.debug("[BasicSseService] 만료된 SseEmiter 객체 삭제 - receiverId={}, emitter={}",
                        receiverId, emitter);
                }
            }
        });

        log.debug("[BasicSseService] 만료된 SseEmitter 객체 삭제 완료");
    }

    @Override
    public boolean ping(SseEmitter sseEmitter) {
        log.debug("[BasicSseService] ping() 호출됨. Emitter hashCode: {}", sseEmitter.hashCode());

        try {
            // 더미 데이터
            sseEmitter.send(
                SseEmitter.event()
                    .id(UUID.randomUUID().toString())
                    .name("ping")
                    .build()
            );
            log.debug("[BasicSseService] ping() - sseEmitter.send() 성공. Emitter hashCode: {}",
                sseEmitter.hashCode());
            return true;
        } catch (Exception e) {
            log.error("[BasicSseService] Ping 실패: Emitter 종료됨. 에러: {}", e.getMessage(), e);
            sseEmitter.complete();
            return false;
        }
    }

    private boolean safeSend(UUID receiverId, SseEmitter sseEmitter, SseMessage msg) {
        try {
            String jsonData = objectMapper.writeValueAsString(msg.event());

            sseEmitter.send(SseEmitter.event()
                .id(msg.id().toString())
                .name(msg.eventName())
                .data(jsonData));
            return true;
        } catch (IOException e) {
            log.warn("[SSE] 전송 실패 → emitter 종료: {}", e.getMessage());
            try {
                sseEmitter.completeWithError(e);
            } catch (Exception ignore) {
            }
            emitterRepository.remove(receiverId, sseEmitter);
            return false;
        }
    }
}
