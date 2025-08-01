package com.vn.caro_game.repositories;

import com.vn.caro_game.entities.GameHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {
    
    /**
     * Find all game history for a specific user (either winner or loser) with pagination
     */
    @Query("SELECT gh FROM GameHistory gh WHERE gh.winnerId = :userId OR gh.loserId = :userId")
    Page<GameHistory> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find all game history for a specific user (either winner or loser) - list version
     */
    @Query("SELECT gh FROM GameHistory gh WHERE gh.winnerId = :userId OR gh.loserId = :userId ORDER BY gh.gameEndedAt DESC")
    List<GameHistory> findByUserIdList(@Param("userId") Long userId);
    
    /**
     * Find game history for a specific room
     */
    List<GameHistory> findByRoomIdOrderByGameEndedAtDesc(Long roomId);
    
    /**
     * Get win count for a user
     */
    @Query("SELECT COUNT(gh) FROM GameHistory gh WHERE gh.winnerId = :userId")
    Long getWinCountByUserId(@Param("userId") Long userId);
    
    /**
     * Get loss count for a user
     */
    @Query("SELECT COUNT(gh) FROM GameHistory gh WHERE gh.loserId = :userId")
    Long getLossCountByUserId(@Param("userId") Long userId);
}
