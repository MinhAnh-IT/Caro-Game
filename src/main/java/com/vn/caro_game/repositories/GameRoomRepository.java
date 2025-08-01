package com.vn.caro_game.repositories;

import com.vn.caro_game.entities.GameRoom;
import com.vn.caro_game.enums.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for GameRoom entity operations.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
    
    /**
     * Finds rooms by status and privacy setting.
     */
    List<GameRoom> findByStatusAndIsPrivateFalse(RoomStatus status);
    
    /**
     * Finds a room by its join code.
     * 
     * @param joinCode the join code to search for
     * @return Optional containing the room if found
     */
    Optional<GameRoom> findByJoinCode(String joinCode);
    
    /**
     * Finds rooms created by a specific user.
     */
    List<GameRoom> findByCreatedBy_Id(Long userId);
    
    /**
     * Finds rooms where a user is a player.
     * 
     * @param userId the user ID to search for
     * @return list of rooms where the user is a player
     */
    @Query("SELECT gr FROM GameRoom gr JOIN gr.roomPlayers rp WHERE rp.user.id = :userId")
    List<GameRoom> findRoomsByUserId(@Param("userId") Long userId);
    
    /**
     * Finds available rooms for a user.
     */
    @Query("SELECT gr FROM GameRoom gr WHERE gr.status = :status AND " +
           "(gr.isPrivate = false OR gr.createdBy.id = :userId)")
    List<GameRoom> findAvailableRoomsForUser(@Param("status") RoomStatus status, 
                                            @Param("userId") Long userId);

    /**
     * Finds all public rooms that are waiting for players with pagination.
     * 
     * @param pageable pagination information
     * @return page of public rooms waiting for players
     */
    @Query("SELECT r FROM GameRoom r WHERE r.isPrivate = false " +
           "AND r.status = com.vn.caro_game.enums.RoomStatus.WAITING " +
           "ORDER BY r.createdAt DESC")
    Page<GameRoom> findPublicWaitingRooms(Pageable pageable);

    /**
     * Finds rooms created by a specific user with pagination.
     * 
     * @param userId the user ID who created the rooms
     * @param pageable pagination information
     * @return page of rooms created by the user
     */
    @Query("SELECT r FROM GameRoom r WHERE r.createdBy.id = :userId " +
           "ORDER BY r.createdAt DESC")
    Page<GameRoom> findByCreatedById(@Param("userId") Long userId, Pageable pageable);

    /**
     * Finds rooms where a user is a player with pagination.
     * 
     * @param userId the user ID to search for
     * @param pageable pagination information
     * @return page of rooms where the user is a player
     */
    @Query("SELECT DISTINCT r FROM GameRoom r " +
           "JOIN r.roomPlayers rp " +
           "WHERE rp.user.id = :userId " +
           "ORDER BY r.createdAt DESC")
    Page<GameRoom> findRoomsByUserIdPaged(@Param("userId") Long userId, Pageable pageable);

    /**
     * Checks if a user is currently in any active room (WAITING or PLAYING).
     * 
     * @param userId the user ID to check
     * @return true if user is in an active room, false otherwise
     */
    @Query("SELECT COUNT(r) > 0 FROM GameRoom r " +
           "JOIN r.roomPlayers rp " +
           "WHERE rp.user.id = :userId " +
           "AND r.status IN (com.vn.caro_game.enums.RoomStatus.WAITING, com.vn.caro_game.enums.RoomStatus.PLAYING)")
    boolean existsActiveRoomByUserId(@Param("userId") Long userId);

    /**
     * Finds active rooms for a user (WAITING or PLAYING status).
     * Using Pageable to limit results.
     * 
     * @param userId the user ID
     * @param pageable to limit results to 1
     * @return List containing the active room if found
     */
    @Query("SELECT r FROM GameRoom r " +
           "JOIN r.roomPlayers rp " +
           "WHERE rp.user.id = :userId " +
           "AND r.status IN (com.vn.caro_game.enums.RoomStatus.WAITING, com.vn.caro_game.enums.RoomStatus.PLAYING) " +
           "ORDER BY r.createdAt DESC, r.id DESC")
    List<GameRoom> findActiveRoomsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Counts the number of players in a room.
     * 
     * @param roomId the room ID
     * @return number of players in the room
     */
    @Query("SELECT COUNT(rp) FROM RoomPlayer rp WHERE rp.room.id = :roomId")
    Integer countPlayersByRoomId(@Param("roomId") Long roomId);

    /**
     * Finds the first available public room (waiting status and not full).
     * 
     * @param pageable to limit results to 1
     * @return List containing the first available public room
     */
    @Query("SELECT r FROM GameRoom r " +
           "WHERE r.isPrivate = false " +
           "AND r.status = com.vn.caro_game.enums.RoomStatus.WAITING " +
           "AND (SELECT COUNT(rp) FROM RoomPlayer rp WHERE rp.room.id = r.id) < 2 " +
           "ORDER BY r.createdAt ASC")
    List<GameRoom> findAvailablePublicRooms(Pageable pageable);

    /**
     * Finds rooms in FINISHED status that can be rematched.
     * 
     * @param userId the user ID to search for
     * @return list of finished rooms where user can create rematch
     */
    @Query("SELECT r FROM GameRoom r " +
           "JOIN r.roomPlayers rp " +
           "WHERE rp.user.id = :userId " +
           "AND r.status = com.vn.caro_game.enums.RoomStatus.FINISHED " +
           "AND r.gameState IN (com.vn.caro_game.enums.GameState.FINISHED, " +
           "                   com.vn.caro_game.enums.GameState.ENDED_BY_SURRENDER, " +
           "                   com.vn.caro_game.enums.GameState.ENDED_BY_LEAVE) " +
           "AND (SELECT COUNT(rp2) FROM RoomPlayer rp2 WHERE rp2.room.id = r.id) = 2 " +
           "ORDER BY r.gameEndedAt DESC")
    List<GameRoom> findRematchableRoomsByUserId(@Param("userId") Long userId);

    /**
     * Finds rooms that are waiting for players to be ready.
     * 
     * @return list of rooms in WAITING_FOR_READY state
     */
    @Query("SELECT r FROM GameRoom r " +
           "WHERE r.gameState = com.vn.caro_game.enums.GameState.WAITING_FOR_READY " +
           "ORDER BY r.createdAt ASC")
    List<GameRoom> findRoomsWaitingForReady();

    /**
     * Counts ready players in a room.
     * 
     * @param roomId the room ID
     * @return number of ready players in the room
     */
    @Query("SELECT COUNT(rp) FROM RoomPlayer rp " +
           "WHERE rp.room.id = :roomId " +
           "AND rp.readyState = com.vn.caro_game.enums.PlayerReadyState.READY")
    Long countReadyPlayersByRoomId(@Param("roomId") Long roomId);

    /**
     * Checks if both players in a room are ready.
     * 
     * @param roomId the room ID
     * @return true if both players are ready, false otherwise
     */
    @Query("SELECT (COUNT(rp) = 2 AND " +
           "COUNT(CASE WHEN rp.readyState = com.vn.caro_game.enums.PlayerReadyState.READY THEN 1 END) = 2) " +
           "FROM RoomPlayer rp WHERE rp.room.id = :roomId")
    boolean areBothPlayersReady(@Param("roomId") Long roomId);
}
