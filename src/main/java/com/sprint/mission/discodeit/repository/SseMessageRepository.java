package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.dto.Sse.SseMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

    private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
    private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

    public void save(SseMessage message) {
        messages.put(message.id(), message);
        eventIdQueue.add(message.id());
    }

    public List<SseMessage> findAfter(UUID lastEventId) {

        if (lastEventId == null) {
            return new ArrayList<>();
        }

        List<SseMessage> result = new ArrayList<>();
        boolean found = false;
        for (UUID id : eventIdQueue) {
            if (found) {
                SseMessage msg = messages.get(id);
                if (msg != null) {
                    result.add(msg);
                }
            } else if (id.equals(lastEventId)) {
                found = true;
            }
        }
        return result;
    }
}
