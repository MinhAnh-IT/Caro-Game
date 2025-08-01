package com.vn.caro_game.dtos;

import com.vn.caro_game.enums.FriendStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Response DTO containing friend information")
public class FriendResponseDto {

    @Schema(description = "User ID of the friend", example = "123")
    Long userId;

    @Schema(description = "Username of the friend", example = "john_doe")
    String username;

    @Schema(description = "Display name of the friend", example = "John Doe")
    String displayName;

    @Schema(description = "Avatar URL of the friend", example = "https://example.com/avatar/123.jpg")
    String avatarUrl;

    @Schema(description = "Current friendship status", example = "ACCEPTED")
    FriendStatus status;

    @Schema(description = "Timestamp when the friendship was created", example = "2024-01-15T10:30:00")
    LocalDateTime createdAt;

    @Schema(description = "Whether the friend is currently online", example = "true")
    Boolean isOnline;
}
