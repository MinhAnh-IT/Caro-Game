package com.vn.caro_game.repositories;

import com.vn.caro_game.entities.Friend;
import com.vn.caro_game.enums.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Friend.FriendId> {

    @Query("SELECT f FROM Friend f WHERE f.id.userId = :userId AND f.id.friendId = :friendId")
    Optional<Friend> findByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query("SELECT f FROM Friend f WHERE f.id.userId = :userId AND f.status = :status")
    List<Friend> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") FriendStatus status);

    @Query("SELECT f FROM Friend f WHERE f.id.friendId = :userId AND f.status = :status")
    List<Friend> findByFriendIdAndStatus(@Param("userId") Long userId, @Param("status") FriendStatus status);

    @Query("SELECT f FROM Friend f WHERE " +
           "(f.id.userId = :userId OR f.id.friendId = :userId) AND f.status = :status")
    List<Friend> findAllFriendsWithStatus(@Param("userId") Long userId, @Param("status") FriendStatus status);

    @Query("SELECT DISTINCT f FROM Friend f WHERE " +
           "((f.id.userId = :userId AND f.id.friendId > :userId) OR " +
           "(f.id.friendId = :userId AND f.id.userId < :userId)) AND f.status = :status")
    List<Friend> findUniqueFriendsWithStatus(@Param("userId") Long userId, @Param("status") FriendStatus status);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friend f " +
           "WHERE ((f.id.userId = :userId AND f.id.friendId = :friendId) OR " +
           "(f.id.userId = :friendId AND f.id.friendId = :userId)) AND f.status IN (:statuses)")
    boolean existsFriendshipWithStatuses(@Param("userId") Long userId,
                                       @Param("friendId") Long friendId,
                                       @Param("statuses") List<FriendStatus> statuses);

    @Query("SELECT f FROM Friend f WHERE (f.id.userId = :userId OR f.id.friendId = :userId) AND f.status = 'ACCEPTED'")
    List<Friend> findAcceptedFriendshipsByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Friend f WHERE f.id.friendId = :userId AND f.status = 'PENDING'")
    List<Friend> findPendingFriendRequestsForUser(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friend f " +
           "WHERE f.id.userId = :userId AND f.id.friendId = :friendId")
    boolean existsByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);
}
