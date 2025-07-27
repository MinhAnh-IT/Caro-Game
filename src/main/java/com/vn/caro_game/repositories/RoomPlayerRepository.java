package com.vn.caro_game.repositories;

import com.vn.caro_game.entities.RoomPlayer;
import com.vn.caro_game.entities.RoomPlayer.RoomPlayerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomPlayerRepository extends JpaRepository<RoomPlayer, RoomPlayerId> {
    
    List<RoomPlayer> findByRoomId(Long roomId);
    
    List<RoomPlayer> findByUserId(Long userId);
    
    @Query("SELECT rp FROM RoomPlayer rp WHERE rp.room.id = :roomId AND rp.isHost = true")
    Optional<RoomPlayer> findHostByRoomId(@Param("roomId") Long roomId);
    
    @Query("SELECT COUNT(rp) FROM RoomPlayer rp WHERE rp.room.id = :roomId")
    long countPlayersByRoomId(@Param("roomId") Long roomId);
    
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);
}
