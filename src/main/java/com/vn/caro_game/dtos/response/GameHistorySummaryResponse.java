package com.vn.caro_game.dtos.response;

import com.vn.caro_game.enums.GameEndReason;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Simplified response DTO for game history list
 * Contains only essential information for displaying game history
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Game history summary for list display")
public class GameHistorySummaryResponse {
    
    @Schema(description = "Game history ID", example = "1")
    Long gameId;
    
    @Schema(description = "Room ID where game was played", example = "1")
    Long roomId;
    
    @Schema(description = "Room name", example = "Epic Caro Battle")
    String roomName;
    
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
    
    @Schema(description = "Game result (WIN, LOSE, DRAW)", example = "WIN")
    String gameResult;
    
    @Schema(description = "Whether current user was the winner", example = "true")
    Boolean isUserWinner;
    
    @Schema(description = "Opponent player name", example = "John Doe")
    String opponentName;
    
    @Schema(description = "Opponent avatar URL", example = "https://example.com/avatar.jpg")
    String opponentAvatar;
}
