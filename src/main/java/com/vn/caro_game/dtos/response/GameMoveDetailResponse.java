package com.vn.caro_game.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Response DTO for detailed game replay moves
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Game move details for replay")
public class GameMoveDetailResponse {
    
    @Schema(description = "Move ID", example = "1")
    Long moveId;
    
    @Schema(description = "Player ID who made the move", example = "1")
    Long playerId;
    
    @Schema(description = "Player name who made the move", example = "john_doe")
    String playerName;
    
    @Schema(description = "Player symbol (X or O)", example = "X")
    String playerSymbol;
    
    @Schema(description = "X position (row) on the board", example = "7")
    Integer xPosition;
    
    @Schema(description = "Y position (column) on the board", example = "8")
    Integer yPosition;
    
    @Schema(description = "Move number in sequence", example = "5")
    Integer moveNumber;
    
    @Schema(description = "Timestamp when move was made", example = "2025-08-03T10:30:00")
    LocalDateTime moveTime;
    
    @Schema(description = "Time taken to make this move in seconds", example = "15")
    Integer moveTimeSeconds;
    
    @Schema(description = "Whether this move resulted in a win", example = "false")
    Boolean isWinningMove;
    
    @Schema(description = "Board state after this move (as 2D array)", example = "[[0,0,1],[1,2,0],[0,0,2]]")
    int[][] boardState;
}
