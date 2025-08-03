package com.vn.caro_game.services.interfaces;

import com.vn.caro_game.dtos.response.GameReplayResponse;
import com.vn.caro_game.dtos.response.GameStatisticsResponse;
import com.vn.caro_game.dtos.response.GameHistorySummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for game statistics and history management
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
public interface GameStatisticsService {
    
    /**
     * Gets comprehensive game statistics for a user
     * 
     * @param userId the user ID
     * @return comprehensive game statistics
     */
    GameStatisticsResponse getUserGameStatistics(Long userId);
    
    /**
     * Gets detailed game replay with all moves for a specific game
     * 
     * @param gameId the game ID from game history
     * @param userId the requesting user ID
     * @return detailed game replay with moves
     */
    GameReplayResponse getGameReplay(Long gameId, Long userId);
    
    /**
     * Gets game replays for a user with pagination (simplified for list display)
     * 
     * @param userId the user ID
     * @param pageable pagination information
     * @return paginated game history summaries
     */
    Page<GameHistorySummaryResponse> getUserGameReplays(Long userId, Pageable pageable);
    
    /**
     * Gets top players by win rate
     * 
     * @param limit number of top players to return
     * @return list of top players with their statistics
     */
    Page<GameStatisticsResponse> getTopPlayersByWinRate(int limit, Pageable pageable);
    
    /**
     * Gets user ranking among all players
     * 
     * @param userId the user ID
     * @return user's ranking position
     */
    Long getUserRanking(Long userId);
}
