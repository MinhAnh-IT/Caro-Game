package com.vn.caro_game.repositories;

import com.vn.caro_game.entities.Friend;
import com.vn.caro_game.entities.Friend.FriendId;
import com.vn.caro_game.enums.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friend, FriendId> {
    
    @Query("SELECT f FROM Friend f WHERE f.user.id = :userId AND f.status = :status")
    List<Friend> findByUserIdAndStatus(@Param("userId") Long userId, 
                                      @Param("status") FriendStatus status);
    
    @Query("SELECT f FROM Friend f WHERE f.friend.id = :userId AND f.status = :status")
    List<Friend> findByFriendIdAndStatus(@Param("userId") Long userId, 
                                        @Param("status") FriendStatus status);
    
    @Query("SELECT f FROM Friend f WHERE " +
           "(f.user.id = :userId OR f.friend.id = :userId) AND f.status = 'ACCEPTED'")
    List<Friend> findAcceptedFriendshipsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT f FROM Friend f WHERE f.friend.id = :userId AND f.status = 'PENDING'")
    List<Friend> findPendingFriendRequestsForUser(@Param("userId") Long userId);
    
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
}
