package com.vn.caro_game.repositories;

import com.vn.caro_game.entities.GameMatch;
import com.vn.caro_game.enums.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameMatchRepository extends JpaRepository<GameMatch, Long> {
    
    List<GameMatch> findByRoomId(Long roomId);
    
    @Query("SELECT gm FROM GameMatch gm WHERE gm.playerX.id = :userId OR gm.playerO.id = :userId")
    List<GameMatch> findByPlayerId(@Param("userId") Long userId);
    
    @Query("SELECT gm FROM GameMatch gm WHERE gm.room.id = :roomId AND gm.result = :result")
    Optional<GameMatch> findByRoomIdAndResult(@Param("roomId") Long roomId, 
                                            @Param("result") GameResult result);
    
    @Query("SELECT gm FROM GameMatch gm WHERE (gm.playerX.id = :userId OR gm.playerO.id = :userId) " +
           "AND gm.result = :result ORDER BY gm.startTime DESC")
    List<GameMatch> findByPlayerIdAndResult(@Param("userId") Long userId, 
                                          @Param("result") GameResult result);
    
    @Query("SELECT COUNT(gm) FROM GameMatch gm WHERE gm.playerX.id = :userId AND gm.result = 'X_WIN'")
    long countWinsByPlayerX(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(gm) FROM GameMatch gm WHERE gm.playerO.id = :userId AND gm.result = 'O_WIN'")
    long countWinsByPlayerO(@Param("userId") Long userId);
}
