package com.vn.caro_game.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Simple Response DTO for game moves in replay
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Simple game move for replay display")
public class SimpleMoveResponse {
    
    @Schema(description = "Move ID", example = "1")
    Long moveId;
    
    @Schema(description = "Player ID who made the move", example = "1")
    Long playerId;
    
    @Schema(description = "Player name who made the move", example = "john_doe")
    String playerName;
    
    @Schema(description = "Player symbol (X or O)", example = "X")
    String playerSymbol;
    
    @Schema(description = "X position (row) on the board", example = "7")
    @JsonProperty("xPosition")
    Integer xPosition;
    
    @Schema(description = "Y position (column) on the board", example = "8")
    @JsonProperty("yPosition")
    Integer yPosition;
    
    @Schema(description = "Move number in sequence", example = "5")
    Integer moveNumber;
    
    @Schema(description = "Timestamp when move was made", example = "2025-08-03T10:30:00")
    LocalDateTime moveTime;
}
