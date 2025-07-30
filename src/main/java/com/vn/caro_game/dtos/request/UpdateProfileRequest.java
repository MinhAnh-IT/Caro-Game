package com.vn.caro_game.dtos.request;

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

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
    @Schema(description = "Username for the user", example = "john_doe123")
    String username;

    @Size(max = 50, message = "Display name cannot exceed 50 characters")
    @Schema(description = "Display name for the user", example = "John Doe")
    String displayName;
}
