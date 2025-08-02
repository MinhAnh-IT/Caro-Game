package com.vn.caro_game.dtos.response;

import com.vn.caro_game.enums.GameResult;
import com.vn.caro_game.enums.GameState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Response DTO for game move operations.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Response data for game move operations")
public class GameMoveResponse {
    
    @Schema(description = "Game room ID", example = "1")
    Long roomId;
    
    @Schema(description = "Game match ID", example = "1")
    Long matchId;
    
    @Schema(description = "X coordinate of the move", example = "7")
    Integer xPosition;
    
    @Schema(description = "Y coordinate of the move", example = "7")
    Integer yPosition;
    
    @Schema(description = "ID of the player who made the move", example = "1")
    Long playerId;
    
    @Schema(description = "Symbol of the player who made the move", example = "X")
    String playerSymbol;
    
    @Schema(description = "Move number in the game sequence", example = "1")
    Integer moveNumber;
    
    @Schema(description = "ID of the player whose turn is next", example = "2")
    Long nextTurnPlayerId;
    
    @Schema(description = "Current game state")
    GameState gameState;
    
    @Schema(description = "Game result if game has ended")
    GameResult gameResult;
    
    @Schema(description = "ID of the winner if game has ended", example = "1")
    Long winnerId;
    
    @Schema(description = "Current board state as 15x15 matrix")
    int[][] board;
    
    @Schema(description = "Whether the move was valid", example = "true")
    Boolean isValidMove;
    
    @Schema(description = "Message describing the move result", example = "Move successful")
    String message;
    
    @Schema(description = "Timestamp of when the move was made", example = "2025-01-01T10:00:00")
    String timestamp;
}
