package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.Common.ApiErrorResponse;
import com.sprint.mission.discodeit.dto.Jwt.JwtDto;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.dto.User.UserRoleUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApi {

    @Operation(summary = "CSRF 토큰 발급")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "CSRF 토큰이 성공적으로 발급됨"
        )
    })
    ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken);

    @Operation(summary = "사용자 권한 변경")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "사용자 권한이 성공적으로 변경됨",
            content = @Content(schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(examples = @ExampleObject(value = "User not found"))
        )
    })
    ResponseEntity<UserDto> updateUserRole(
        @Valid @RequestBody UserRoleUpdateRequest roleUpdateRequest
    );

    @Operation(summary = "Access Token 재발급")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "재발급 성공",
            content = @Content(schema = @Schema(implementation = JwtDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "유효하지 않은 Refresh Token",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "토큰 재발급 중 서버 오류",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
    })
    ResponseEntity<?> refreshAccessToken(
        String refreshToken,
        HttpServletResponse response
    );
}