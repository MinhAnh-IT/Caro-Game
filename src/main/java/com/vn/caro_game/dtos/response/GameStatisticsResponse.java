package com.vn.caro_game.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Response DTO for game statistics
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Game statistics information")
public class GameStatisticsResponse {
    
    @Schema(description = "User ID", example = "1")
    Long userId;
    
    @Schema(description = "Total games played", example = "25")
    Long totalGamesPlayed;
    
    @Schema(description = "Number of games won", example = "15")
    Long totalWins;
    
    @Schema(description = "Number of games lost", example = "8")
    Long totalLosses;
    
    @Schema(description = "Number of games drawn", example = "2")
    Long totalDraws;
    
    @Schema(description = "Win rate percentage", example = "60.0")
    Double winRate;
    
    @Schema(description = "Loss rate percentage", example = "32.0")
    Double lossRate;
    
    @Schema(description = "Draw rate percentage", example = "8.0")
    Double drawRate;
    
    @Schema(description = "Total game time in minutes", example = "1250")
    Long totalGameTimeMinutes;
    
    @Schema(description = "Average game duration in minutes", example = "50")
    Double averageGameDurationMinutes;
    
    @Schema(description = "Longest game duration in minutes", example = "120")
    Long longestGameDurationMinutes;
    
    @Schema(description = "Shortest game duration in minutes", example = "15")
    Long shortestGameDurationMinutes;
    
    @Schema(description = "Current winning streak", example = "3")
    Integer currentWinStreak;
    
    @Schema(description = "Best winning streak", example = "7")
    Integer bestWinStreak;
    
    @Schema(description = "Player ranking", example = "Advanced")
    String playerRank;
    
    @Schema(description = "Total score/points", example = "1580")
    Long totalScore;
}
