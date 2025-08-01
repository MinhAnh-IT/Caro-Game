package com.vn.caro_game.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Response DTO for user summary information.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "User summary information")
public class UserSummaryResponse {

    @Schema(description = "User ID", example = "1")
    Long id;

    @Schema(description = "User display name", example = "John Doe")
    String displayName;

    @Schema(description = "User avatar URL", example = "https://example.com/avatar.jpg")
    String avatarUrl;

    @Schema(description = "Username", example = "johndoe")
    String username;
}
