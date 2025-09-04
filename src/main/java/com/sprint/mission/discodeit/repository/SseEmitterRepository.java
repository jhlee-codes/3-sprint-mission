package com.sprint.mission.discodeit.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

    private final ConcurrentHashMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

    /**
     * SseEmitter 추가 (CopyOnWriteArrayList를 활용해 동시성 문제 해결)
     */
    public void add(UUID receiverId, SseEmitter sseEmitter) {
        data.computeIfAbsent(receiverId, k -> new CopyOnWriteArrayList<>()).add(sseEmitter);
    }

    /**
     * SseEmitter 제거
     */
    public void remove(UUID receiverId, SseEmitter sseEmitter) {
        List<SseEmitter> emitters = data.get(receiverId);
        if (emitters != null) {
            emitters.remove(sseEmitter);
            if (emitters.isEmpty()) {
                data.remove(receiverId);
            }
        }
    }

    /**
     * 특정 수신자의 모든 SseEmitter를 가져옴
     */
    public List<SseEmitter> get(UUID receiverId) {
        return data.getOrDefault(receiverId, new ArrayList<>());
    }

    /**
     * 모든 SseEmitter를 포함하는 Map 반환
     */
    public Map<UUID, List<SseEmitter>> findAll() {
        return data;
    }
}
