package com.vn.caro_game.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for making a game move in Caro game.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request data for making a game move")
public class GameMoveRequest {
    
    @Schema(description = "X coordinate of the move (0-14)", example = "7", minimum = "0", maximum = "14")
    @NotNull(message = "X position is required")
    @Min(value = 0, message = "X position must be between 0 and 14")
    @Max(value = 14, message = "X position must be between 0 and 14")
    @JsonProperty("xPosition")
    Integer xPosition;
    
    @Schema(description = "Y coordinate of the move (0-14)", example = "7", minimum = "0", maximum = "14")
    @NotNull(message = "Y position is required")
    @Min(value = 0, message = "Y position must be between 0 and 14")
    @Max(value = 14, message = "Y position must be between 0 and 14")
    @JsonProperty("yPosition")
    Integer yPosition;
}
