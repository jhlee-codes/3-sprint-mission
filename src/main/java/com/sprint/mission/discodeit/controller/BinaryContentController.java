package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.BinaryContentApi;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/binaryContents")
@RestController
public class BinaryContentController implements BinaryContentApi {

    private final BinaryContentService binaryContentService;

    /**
     * 파이너리 파일 단건 조회
     *
     * @param binaryContentId 조회할 바이너리 파일 ID
     * @return 조회된 바이너리 파일 (HTTP 200 OK)
     */
    @GetMapping(path = "/{binaryContentId}")
    @Override
    public ResponseEntity<BinaryContent> find(
            @PathVariable UUID binaryContentId
    ) {
        BinaryContent content = binaryContentService.find(binaryContentId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(content);
    }

    /**
     * 바이너리 파일 1개 또는 여러 개 조회
     *
     * @param binaryContentIds 조회할 바이너리 파일 ID 목록
     * @return 조회된 바이너리 파일 목록 (HTTP 200 OK)
     */
    @GetMapping
    @Override
    public ResponseEntity<List<BinaryContent>> findAllByIdIn(
            @RequestParam("binaryContentIds") List<UUID> binaryContentIds
    ) {
        List<BinaryContent> contents = binaryContentService.findAllByIdIn(binaryContentIds);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contents);
    }
}
