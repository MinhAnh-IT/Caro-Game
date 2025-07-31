package com.vn.caro_game.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * DTO for user profile information response.
 *
 * <p>This response DTO contains user profile information that can be safely
 * shared with clients. It excludes sensitive information like password hashes.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "User profile information response")
public class UserProfileResponse {

    @Schema(
        description = "Unique identifier for the user account",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Long id;

    @Schema(
        description = "Unique username for login and identification",
        example = "john_doe123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String username;

    @Schema(
        description = "User's email address for communication and login",
        example = "john@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String email;

    @Schema(
        description = "Display name shown to other users in the application",
        example = "John Doe",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String displayName;

    @Schema(
        description = "URL path to user's avatar image. Null if no avatar uploaded.",
        example = "/uploads/avatars/user_1_avatar.jpg",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String avatarUrl;

    @Schema(
        description = "Timestamp when the user account was created",
        example = "2024-01-01T10:00:00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    LocalDateTime createdAt;
}
