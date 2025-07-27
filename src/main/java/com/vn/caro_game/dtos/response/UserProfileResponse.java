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

    @Schema(description = "User ID", example = "1")
    Long id;

    @Schema(description = "Username", example = "john_doe123")
    String username;

    @Schema(description = "Email address", example = "john@example.com")
    String email;

    @Schema(description = "Display name", example = "John Doe")
    String displayName;

    @Schema(description = "Avatar URL", example = "/uploads/avatars/user_1_avatar.jpg")
    String avatarUrl;

    @Schema(description = "Account creation timestamp", example = "2024-01-01T10:00:00")
    LocalDateTime createdAt;
}
