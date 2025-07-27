package com.vn.caro_game.repositories;

import com.vn.caro_game.entities.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findByRoomIdOrderBySentAtAsc(Long roomId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.room.id = :roomId " +
           "AND cm.sentAt >= :since ORDER BY cm.sentAt ASC")
    List<ChatMessage> findByRoomIdAndSentAtAfter(@Param("roomId") Long roomId, 
                                                @Param("since") LocalDateTime since);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.room.id = :roomId " +
           "ORDER BY cm.sentAt DESC LIMIT :limit")
    List<ChatMessage> findLatestMessagesByRoomId(@Param("roomId") Long roomId, 
                                                @Param("limit") int limit);
    
    List<ChatMessage> findBySenderIdOrderBySentAtDesc(Long senderId);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.room.id = :roomId")
    long countMessagesByRoomId(@Param("roomId") Long roomId);
}
