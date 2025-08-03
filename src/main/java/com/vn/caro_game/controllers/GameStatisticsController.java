package com.vn.caro_game.controllers;

import com.vn.caro_game.controllers.base.BaseController;
import com.vn.caro_game.dtos.response.ApiResponse;
import com.vn.caro_game.dtos.response.GameReplayResponse;
import com.vn.caro_game.dtos.response.GameStatisticsResponse;
import com.vn.caro_game.dtos.response.GameHistorySummaryResponse;
import com.vn.caro_game.configs.CustomUserDetails;
import com.vn.caro_game.services.interfaces.GameStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Game Statistics and History operations
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Tag(name = "Game Statistics", description = "Game statistics and history management APIs")
@RestController
@RequestMapping("/api/statistics")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class GameStatisticsController extends BaseController {

    private final GameStatisticsService gameStatisticsService;

    /**
     * Gets comprehensive game statistics for the current user
     */
    @Operation(summary = "Get user game statistics", 
              description = "Get comprehensive game statistics including wins, losses, draws, win rate, and other metrics")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/my-stats")
    public ResponseEntity<ApiResponse<GameStatisticsResponse>> getMyGameStatistics(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        GameStatisticsResponse statistics = gameStatisticsService.getUserGameStatistics(userDetails.getUserId());
        return success(statistics, "Game statistics retrieved successfully");
    }

    /**
     * Gets game statistics for a specific user (public data)
     */
    @Operation(summary = "Get user game statistics by ID", 
              description = "Get public game statistics for any user")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<GameStatisticsResponse>> getUserGameStatistics(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long userId) {
        
        GameStatisticsResponse statistics = gameStatisticsService.getUserGameStatistics(userId);
        return success(statistics, "User game statistics retrieved successfully");
    }

    /**
     * Gets detailed game replay with all moves
     */
    @Operation(summary = "Get game replay", 
              description = "Get detailed game replay with all moves for a specific game using gameId from history")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Game replay retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - user was not part of this game"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Game not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/replay/{gameId}")
    public ResponseEntity<ApiResponse<GameReplayResponse>> getGameReplay(
            @Parameter(description = "Game ID from game history", example = "1")
            @PathVariable Long gameId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        GameReplayResponse replay = gameStatisticsService.getGameReplay(gameId, userDetails.getUserId());
        return success(replay, "Game replay retrieved successfully");
    }

    /**
     * Gets user's game history with pagination
     */
    @Operation(summary = "Get user game history", 
              description = "Get paginated list of user's game history")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Game history retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<Page<GameHistorySummaryResponse>>> getMyGameHistory(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDirection,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "gameEndedAt"));
        
        Page<GameHistorySummaryResponse> history = gameStatisticsService.getUserGameReplays(userDetails.getUserId(), pageable);
        return success(history, "Game history retrieved successfully");
    }

    /**
     * Gets top players by win rate
     */
    @Operation(summary = "Get top players by win rate", 
              description = "Get leaderboard of top players based on win rate")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Top players retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<Page<GameStatisticsResponse>>> getTopPlayers(
            @Parameter(description = "Number of top players to return", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<GameStatisticsResponse> topPlayers = gameStatisticsService.getTopPlayersByWinRate(limit, pageable);
        return success(topPlayers, "Top players retrieved successfully");
    }

    /**
     * Gets user's ranking among all players
     */
    @Operation(summary = "Get user ranking", 
              description = "Get user's ranking position among all players")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User ranking retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found or not enough games"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/my-ranking")
    public ResponseEntity<ApiResponse<Long>> getMyRanking(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long ranking = gameStatisticsService.getUserRanking(userDetails.getUserId());
        if (ranking == null) {
            return success(null, "Not enough games played to determine ranking (minimum 5 games required)");
        }
        return success(ranking, "User ranking retrieved successfully");
    }

}
