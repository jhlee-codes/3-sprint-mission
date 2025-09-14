package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.dto.Notification.NotificationDto;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Notification", description = "알림 API")
public interface NotificationApi {

    @Operation(
        summary = "알림 조회",
        description = "현재 사용자(Access Token)의 알림 목록을 조회합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "성공",
                content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = NotificationDto.class)))),
            @ApiResponse(
                responseCode = "401",
                description = "인증 실패",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))
            )
        }
    )
    ResponseEntity<List<NotificationDto>> getMyNotifications(
        @Parameter(hidden = true)
        @AuthenticationPrincipal DiscodeitUserDetails me
    );

    @Operation(
        summary = "알림 확인(삭제)",
        description = "요청자 본인의 알림에 대해서만 삭제(확인)할 수 있습니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공(응답 본문 없음)"),
            @ApiResponse(
                responseCode = "401",
                description = "인증 실패",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "인가 실패(본인 알림 아님)",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "알림 없음",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))
            )
        }
    )
    ResponseEntity<Void> confirm(
        @Parameter(description = "확인할 Notification ID") UUID notificationId
    );
}
