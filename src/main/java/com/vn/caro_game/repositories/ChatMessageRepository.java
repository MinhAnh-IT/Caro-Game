package com.vn.caro_game.repositories;

import com.vn.caro_game.entities.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for ChatMessage entity operations.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    /**
     * Finds all chat messages in a room ordered by sent time ascending.
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.room.id = :roomId ORDER BY cm.sentAt ASC")
    List<ChatMessage> findByRoomIdOrderBySentAtAsc(@Param("roomId") Long roomId);
    
    /**
     * Finds chat messages in a room after a specific time.
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.room.id = :roomId " +
           "AND cm.sentAt >= :since ORDER BY cm.sentAt ASC")
    List<ChatMessage> findByRoomIdAndSentAtAfter(@Param("roomId") Long roomId, 
                                                @Param("since") LocalDateTime since);
    
    /**
     * Finds the latest chat messages in a room.
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.room.id = :roomId " +
           "ORDER BY cm.sentAt DESC LIMIT :limit")
    List<ChatMessage> findLatestMessagesByRoomId(@Param("roomId") Long roomId, 
                                                @Param("limit") int limit);
    
    /**
     * Finds messages by sender ordered by sent time descending.
     */
    List<ChatMessage> findBySenderIdOrderBySentAtDesc(Long senderId);
    
    /**
     * Counts the number of messages in a room.
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.room.id = :roomId")
    long countMessagesByRoomId(@Param("roomId") Long roomId);

    /**
     * Finds chat messages in a specific room with pagination.
     * 
     * @param roomId the room ID
     * @param pageable pagination information
     * @return page of chat messages ordered by sent time descending
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.room.id = :roomId ORDER BY cm.sentAt DESC")
    Page<ChatMessage> findByRoomIdOrderBySentAtDesc(@Param("roomId") Long roomId, Pageable pageable);

    /**
     * Counts the number of messages in a room using Long return type.
     * 
     * @param roomId the room ID
     * @return number of messages in the room
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.room.id = :roomId")
    Long countByRoomId(@Param("roomId") Long roomId);
}
