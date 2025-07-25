package com.vn.caro_game.repositories;

import com.vn.caro_game.entities.GameRoom;
import com.vn.caro_game.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
    
    List<GameRoom> findByStatusAndIsPrivateFalse(RoomStatus status);
    
    Optional<GameRoom> findByJoinCode(String joinCode);
    
    List<GameRoom> findByCreatedBy_Id(Long userId);
    
    @Query("SELECT gr FROM GameRoom gr JOIN gr.roomPlayers rp WHERE rp.user.id = :userId")
    List<GameRoom> findRoomsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT gr FROM GameRoom gr WHERE gr.status = :status AND " +
           "(gr.isPrivate = false OR gr.createdBy.id = :userId)")
    List<GameRoom> findAvailableRoomsForUser(@Param("status") RoomStatus status, 
                                            @Param("userId") Long userId);
}
