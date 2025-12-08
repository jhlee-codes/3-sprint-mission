package com.sprint.mission.discodeit.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Repository
public class SseEmitterRepository {

    private final ConcurrentHashMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

    /**
     * SseEmitter 추가 (CopyOnWriteArrayList를 활용해 동시성 문제 해결)
     */
    public void add(UUID receiverId, SseEmitter sseEmitter) {
        log.debug("[SseEmitterRepository] add() 호출 전 - data 맵 크기: {}", data.size());
        data.computeIfAbsent(receiverId, k -> new CopyOnWriteArrayList<>()).add(sseEmitter);
        log.debug("[SseEmitterRepository] Emitter 추가: receiverId={}, 현재 Emitter 수: {}", receiverId,
            data.get(receiverId).size());
        log.debug("[SseEmitterRepository] add() 호출 후 - data 맵 크기: {}", data.size());
    }

    /**
     * SseEmitter 제거
     */
    public void remove(UUID receiverId, SseEmitter sseEmitter) {
        log.debug("[SseEmitterRepository] remove() 호출 전 - data 맵 크기: {}", data.size());
        List<SseEmitter> emitters = data.get(receiverId);
        if (emitters != null) {
            log.debug("[SseEmitterRepository] remove() - receiverId {}의 Emitter 리스트 크기: {}",
                receiverId, emitters.size());
            emitters.remove(sseEmitter);
            if (emitters.isEmpty()) {
                data.remove(receiverId);
                log.debug(
                    "[SseEmitterRepository] remove() - receiverId {}의 Emitter 리스트가 비어 맵에서 제거됨",
                    receiverId);
            }
        }
        log.debug("[SseEmitterRepository] remove() 호출 후 - data 맵 크기: {}", data.size());
    }

    /**
     * 특정 수신자의 모든 SseEmitter를 가져옴
     */
    public List<SseEmitter> get(UUID receiverId) {
        List<SseEmitter> emitters = data.getOrDefault(receiverId, new ArrayList<>());
        return emitters;
    }

    /**
     * 모든 SseEmitter를 포함하는 Map 반환
     */
    public Map<UUID, List<SseEmitter>> findAll() {
        log.debug("[SseEmitterRepository] findAll() 호출 - data 맵 크기: {}", data.size());
        return data;
    }
}
