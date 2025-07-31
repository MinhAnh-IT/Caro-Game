package com.vn.caro_game.services.interfaces;

import com.vn.caro_game.dtos.FriendResponseDto;
import com.vn.caro_game.dtos.UserSearchResponseDto;

import java.util.List;

/**
 * Interface for Friend management service
 * Provides methods for friend requests, friendship management, and user search
 */
public interface IFriendService {

    /**
     * Search users by display name or username
     * @param searchTerm the search term to look for
     * @param currentUserId the ID of the current user
     * @return list of users matching the search criteria
     */
    List<UserSearchResponseDto> searchUsers(String searchTerm, Long currentUserId);

    /**
     * Send friend request from one user to another
     * @param fromUserId the ID of the user sending the request
     * @param toUserId the ID of the user receiving the request
     */
    void sendFriendRequest(Long fromUserId, Long toUserId);

    /**
     * Accept a friend request
     * @param userId the ID of the user accepting the request
     * @param fromUserId the ID of the user who sent the request
     */
    void acceptFriendRequest(Long userId, Long fromUserId);

    /**
     * Reject a friend request
     * @param userId the ID of the user rejecting the request
     * @param fromUserId the ID of the user who sent the request
     */
    void rejectFriendRequest(Long userId, Long fromUserId);

    /**
     * Get list of friends for a user
     * @param userId the ID of the user
     * @return list of friends
     */
    List<FriendResponseDto> getFriendsList(Long userId);

    /**
     * Get list of pending friend requests received by a user
     * @param userId the ID of the user
     * @return list of pending friend requests
     */
    List<FriendResponseDto> getPendingFriendRequests(Long userId);

    /**
     * Get list of friend requests sent by a user
     * @param userId the ID of the user
     * @return list of sent friend requests
     */
    List<FriendResponseDto> getSentFriendRequests(Long userId);
}
