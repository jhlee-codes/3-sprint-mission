package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.SseService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class SseController {

    private final SseService sseService;

    @GetMapping(path = "/api/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect(
        @AuthenticationPrincipal DiscodeitUserDetails me,
        @RequestParam(value = "lastEventId", required = false) String lastEventId
    ) {
        UUID receiverId = me.getId();
        log.debug("[SseController] SSE 연결 요청 - receiverId={}, lastEventId={}", receiverId,
            lastEventId);

        if (receiverId == null) {
            log.warn("[SseController] 유효하지 않은 사용자 ID");
            throw new IllegalArgumentException("사용자 ID는 필수 값입니다.");
        }

        UUID last = null;
        if (lastEventId != null && !lastEventId.isEmpty()) {
            last = UUID.fromString(lastEventId);
        }

        try {
            SseEmitter emitter = sseService.connect(receiverId, last);
            log.debug("[SseController] SSE 연결 완료 - receiverId={}", receiverId);

            return ResponseEntity
                .status(HttpStatus.OK)
                .body(emitter);
        } catch (Exception e) {
            log.error("[SseController] SSE 연결 처리 중 오류 발생 - receiverId={}, lastEventId={}",
                receiverId, lastEventId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SSE 연결 실패", e);
        }
    }
}
