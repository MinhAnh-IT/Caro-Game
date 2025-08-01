package com.vn.caro_game.repositories;

import com.vn.caro_game.entities.RoomPlayer;
import com.vn.caro_game.entities.RoomPlayer.RoomPlayerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RoomPlayer entity operations.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Repository
public interface RoomPlayerRepository extends JpaRepository<RoomPlayer, RoomPlayerId> {
    
    /**
     * Finds all players in a specific room by room ID.
     */
    List<RoomPlayer> findByRoomId(Long roomId);
    
    /**
     * Finds all rooms where a user is a player.
     */
    List<RoomPlayer> findByUserId(Long userId);
    
    /**
     * Finds all players in a specific room with room entity.
     * 
     * @param roomId the room ID
     * @return list of room players
     */
    List<RoomPlayer> findByRoom_Id(Long roomId);
    
    /**
     * Finds the host of a room.
     * 
     * @param roomId the room ID
     * @return Optional containing the host if found
     */
    @Query("SELECT rp FROM RoomPlayer rp WHERE rp.room.id = :roomId AND rp.isHost = true")
    Optional<RoomPlayer> findHostByRoomId(@Param("roomId") Long roomId);
    
    /**
     * Counts the number of players in a room.
     * 
     * @param roomId the room ID
     * @return number of players in the room
     */
    @Query("SELECT COUNT(rp) FROM RoomPlayer rp WHERE rp.room.id = :roomId")
    long countPlayersByRoomId(@Param("roomId") Long roomId);
    
    /**
     * Checks if a user is in a specific room.
     */
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    /**
     * Finds a specific room player by room and user IDs.
     * 
     * @param roomId the room ID
     * @param userId the user ID
     * @return Optional containing the room player if found
     */
    @Query("SELECT rp FROM RoomPlayer rp WHERE rp.room.id = :roomId AND rp.user.id = :userId")
    Optional<RoomPlayer> findByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * Deletes a room player by room and user IDs.
     * 
     * @param roomId the room ID
     * @param userId the user ID
     */
    @Modifying
    @Query("DELETE FROM RoomPlayer rp WHERE rp.room.id = :roomId AND rp.user.id = :userId")
    void deleteByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * Counts players in a room using Integer return type.
     * 
     * @param roomId the room ID
     * @return number of players in the room
     */
    Integer countByRoom_Id(Long roomId);
}
