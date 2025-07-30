package com.vn.caro_game.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
 * <p>This request DTO contains fields that users can update for their profile.
 * It includes validation constraints to ensure data integrity.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request object for updating user profile information")
public class UserProfileUpdateRequest {

    @NotBlank(message = "Display name cannot be blank")
    @Size(min = 2, max = 50, message = "Display name must be between 2 and 50 characters")
    @Schema(
        description = "User's display name",
        example = "John Doe",
        minLength = 2,
        maxLength = 50,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String displayName;
}
