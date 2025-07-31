package com.vn.caro_game.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Response DTO for user search results with relationship status")
public class UserSearchResponseDto {

    @Schema(description = "User ID", example = "123")
    Long id;

    @Schema(description = "Username of the user", example = "john_doe")
    String username;

    @Schema(description = "Display name of the user", example = "John Doe")
    String displayName;

    @Schema(description = "Avatar URL of the user", example = "https://example.com/avatar/123.jpg")
    String avatarUrl;

    @Schema(description = "Whether a friend request can be sent to this user", example = "true")
    Boolean canSendRequest;

    @Schema(
        description = "Current relationship status with this user",
        example = "NONE",
        allowableValues = {"NONE", "PENDING", "FRIENDS", "BLOCKED"}
    )
    String relationshipStatus;
}
