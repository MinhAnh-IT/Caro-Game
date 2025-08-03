package com.vn.caro_game.dtos.response;

import com.vn.caro_game.enums.GameEndReason;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for game replay with detailed moves
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Complete game replay information")
public class GameReplayResponse {
    
    @Schema(description = "Game history ID", example = "1")
    Long gameId;
    
    @Schema(description = "Room ID where game was played", example = "1")
    Long roomId;
    
    @Schema(description = "Room name", example = "Quick Play Room")
    String roomName;
    
    @Schema(description = "Player X information")
    PlayerInfo playerX;
    
    @Schema(description = "Player O information")
    PlayerInfo playerO;
    
    @Schema(description = "Winner ID (null for draw)", example = "1")
    Long winnerId;
    
    @Schema(description = "Game end reason", example = "WIN")
    GameEndReason endReason;
    
    @Schema(description = "Game start time", example = "2025-08-03T10:00:00")
    LocalDateTime gameStartTime;
    
    @Schema(description = "Game end time", example = "2025-08-03T10:45:00")
    LocalDateTime gameEndTime;
    
    @Schema(description = "Game duration in minutes", example = "45")
    Long gameDurationMinutes;
    
    @Schema(description = "Total number of moves", example = "23")
    Integer totalMoves;
    
    @Schema(description = "List of all moves in chronological order")
    List<SimpleMoveResponse> moves;
    
    @Schema(description = "Final board state", example = "[[1,0,2],[2,1,0],[0,2,1]]")
    int[][] finalBoardState;
    
    @Schema(description = "Game result (X_WIN, O_WIN, DRAW, ONGOING)", example = "X_WIN")
    String gameResult;
    
    @Schema(description = "Whether current user was the winner", example = "true")
    Boolean isUserWinner;
    
    /**
     * Player information for game replay
     */
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Schema(description = "Player information")
    public static class PlayerInfo {
        
        @Schema(description = "Player ID", example = "1")
        Long playerId;
        
        @Schema(description = "Player name", example = "john_doe")
        String playerName;
        
        @Schema(description = "Player display name", example = "John Doe")
        String displayName;
        
        @Schema(description = "Player avatar URL", example = "https://example.com/avatar.jpg")
        String avatarUrl;
        
        @Schema(description = "Player symbol (X or O)", example = "X")
        String symbol;
        
        @Schema(description = "Total moves made by this player", example = "12")
        Integer moveCount;
        
        @Schema(description = "Average time per move in seconds", example = "18.5")
        Double averageMoveTime;
    }
}
