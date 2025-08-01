package com.vn.caro_game.dtos.request;

import com.vn.caro_game.constants.UserProfileConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * DTO for updating user profile information.
 *
 * <p>This request DTO contains the fields that users can update in their profile.
 * It includes validation constraints to ensure data integrity and consistency.</p>
 *
 * <h3>Validation Rules:</h3>
 * <ul>
 *   <li><strong>Username:</strong> Required, 3-20 characters, alphanumeric and underscore only</li>
 *   <li><strong>Display Name:</strong> Optional, max 50 characters if provided</li>
 * </ul>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request DTO for updating user profile")
public class UpdateProfileRequest {

    @NotBlank(message = UserProfileConstants.USERNAME_REQUIRED)
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
    @Schema(
        description = "Username for the user account. Must be unique across the system.",
        example = "john_doe123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String username;

    @Size(max = 50, message = UserProfileConstants.DISPLAY_NAME_MAX_LENGTH)
    @Schema(
        description = "Display name shown to other users. Can be different from username.",
        example = "John Doe",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String displayName;
}
