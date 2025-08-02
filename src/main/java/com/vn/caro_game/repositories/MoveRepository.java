package com.vn.caro_game.repositories;

import com.vn.caro_game.entities.Move;
import com.vn.caro_game.entities.GameMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoveRepository extends JpaRepository<Move, Long> {
    
    List<Move> findByMatchIdOrderByMoveNumberAsc(Long matchId);
    
    List<Move> findByMatchOrderByMoveNumber(GameMatch match);
    
    int countByMatch(GameMatch match);
    
    @Query("SELECT m FROM Move m WHERE m.match.id = :matchId AND m.player.id = :playerId " +
           "ORDER BY m.moveNumber ASC")
    List<Move> findByMatchIdAndPlayerIdOrderByMoveNumber(@Param("matchId") Long matchId, 
                                                        @Param("playerId") Long playerId);
    
    @Query("SELECT m FROM Move m WHERE m.match.id = :matchId " +
           "ORDER BY m.moveNumber DESC LIMIT 1")
    Move findLastMoveByMatchId(@Param("matchId") Long matchId);
    
    @Query("SELECT COUNT(m) FROM Move m WHERE m.match.id = :matchId")
    long countMovesByMatchId(@Param("matchId") Long matchId);
    
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Move m " +
           "WHERE m.match.id = :matchId AND m.xPosition = :xPosition AND m.yPosition = :yPosition")
    boolean existsByMatchIdAndXPositionAndYPosition(@Param("matchId") Long matchId, 
                                                   @Param("xPosition") Integer xPosition, 
                                                   @Param("yPosition") Integer yPosition);
}
