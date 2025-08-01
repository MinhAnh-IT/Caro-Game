package com.vn.caro_game.dtos.request;

import com.vn.caro_game.constants.GameRoomConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for joining a room by join code.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request data for joining a room with join code")
public class JoinRoomRequest {

    @NotBlank(message = "Join code is required")
    @Pattern(regexp = GameRoomConstants.JOIN_CODE_PATTERN, 
             message = "Join code must be 4 alphanumeric characters")
    @Schema(description = "4-character alphanumeric join code", 
            example = "AB12", 
            pattern = GameRoomConstants.JOIN_CODE_PATTERN)
    String joinCode;
}
