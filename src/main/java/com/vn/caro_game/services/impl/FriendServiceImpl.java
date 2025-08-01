package com.vn.caro_game.services.impl;

import com.vn.caro_game.constants.FriendConstants;
import com.vn.caro_game.dtos.FriendResponseDto;
import com.vn.caro_game.dtos.UserSearchResponseDto;
import com.vn.caro_game.entities.Friend;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.FriendStatus;
import com.vn.caro_game.enums.StatusCode;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.integrations.redis.RedisService;
import com.vn.caro_game.repositories.FriendRepository;
import com.vn.caro_game.repositories.UserRepository;
import com.vn.caro_game.services.interfaces.IFriendService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of friend service operations.
 *
 * <p>This service handles all friend-related business logic including
 * friend requests, acceptance/rejection, and friend list management.
 * It follows Clean Architecture principles and implements proper error handling.</p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>User search functionality</li>
 *   <li>Friend request management</li>
 *   <li>Friend list retrieval</li>
 *   <li>Relationship status tracking</li>
 *   <li>Transactional data consistency</li>
 * </ul>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendServiceImpl implements IFriendService {

    FriendRepository friendRepository;
    UserRepository userRepository;
    RedisService redisService;

    /**
     * Searches for users by display name or username, excluding current user.
     *
     * @param searchTerm the search term for finding users
     * @param currentUserId the ID of the current user to exclude from results
     * @return list of users matching the search criteria
     */
    @Override
    public List<UserSearchResponseDto> searchUsers(String searchTerm, Long currentUserId) {
        log.info("Searching users with term: {} for user: {}", searchTerm, currentUserId);
        
        // Trim whitespace and handle empty search term
        String trimmedSearchTerm = searchTerm != null ? searchTerm.trim() : "";
        
        if (trimmedSearchTerm.isEmpty()) {
            log.info("Empty search term provided, returning empty list");
            return List.of();
        }
        
        List<User> users = userRepository.findUsersByDisplayNameOrUsername(trimmedSearchTerm, currentUserId);

        List<UserSearchResponseDto> result = users.stream()
                .map(user -> mapToUserSearchResponseDto(user, currentUserId))
                .collect(Collectors.toList());
        
        log.info("Found {} users matching search term: {}", result.size(), trimmedSearchTerm);
        return result;
    }

    /**
     * Sends a friend request from one user to another.
     *
     * @param fromUserId the ID of the user sending the request
     * @param toUserId the ID of the user receiving the request
     * @throws CustomException if validation fails or relationship already exists
     */
    @Override
    @Transactional
    public void sendFriendRequest(Long fromUserId, Long toUserId) {
        log.info("Processing friend request from user {} to user {}", fromUserId, toUserId);
        
        validateFriendRequestInput(fromUserId, toUserId);
        validateUserExists(toUserId);
        validateNoExistingRelationship(fromUserId, toUserId);

        Friend friendRequest = createFriendRequest(fromUserId, toUserId);
        friendRepository.save(friendRequest);

        log.info("Friend request sent successfully from user {} to user {}", fromUserId, toUserId);
    }

    /**
     * Accepts a pending friend request.
     *
     * @param userId the ID of the user accepting the request
     * @param fromUserId the ID of the user who sent the request
     * @throws CustomException if friend request not found or invalid status
     */
    @Override
    @Transactional
    public void acceptFriendRequest(Long userId, Long fromUserId) {
        log.info("Processing friend request acceptance: user {} accepting request from user {}", userId, fromUserId);
        
        Friend friendRequest = findPendingFriendRequest(fromUserId, userId);
        validateFriendRequestStatus(friendRequest);

        updateFriendRequestStatus(friendRequest, FriendStatus.ACCEPTED);
        createReverseFriendship(userId, fromUserId);

        log.info("Friend request accepted successfully between user {} and user {}", userId, fromUserId);
    }

    /**
     * Rejects a pending friend request.
     *
     * @param userId the ID of the user rejecting the request
     * @param fromUserId the ID of the user who sent the request
     * @throws CustomException if friend request not found or invalid status
     */
    @Override
    @Transactional
    public void rejectFriendRequest(Long userId, Long fromUserId) {
        log.info("Processing friend request rejection: user {} rejecting request from user {}", userId, fromUserId);
        
        Friend friendRequest = findPendingFriendRequest(fromUserId, userId);
        validateFriendRequestStatus(friendRequest);

        friendRepository.delete(friendRequest);
        log.info("Friend request rejected successfully from user {} to user {}", fromUserId, userId);
    }

    /**
     * Retrieves the list of accepted friends for a user.
     *
     * @param userId the ID of the user to get friends for
     * @return list of friends with accepted status (no duplicates)
     */
    @Override
    public List<FriendResponseDto> getFriendsList(Long userId) {
        log.info("Retrieving friends list for user: {}", userId);
        
        List<Friend> friendships = friendRepository.findUniqueFriendsWithStatus(userId, FriendStatus.ACCEPTED);

        List<FriendResponseDto> result = friendships.stream()
                .map(friendship -> mapToFriendResponseDto(friendship, userId))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        
        log.info("Retrieved {} friends for user: {}", result.size(), userId);
        return result;
    }

    /**
     * Retrieves pending friend requests received by a user.
     *
     * @param userId the ID of the user to get pending requests for
     * @return list of pending friend requests
     */
    @Override
    public List<FriendResponseDto> getPendingFriendRequests(Long userId) {
        log.info("Retrieving pending friend requests for user: {}", userId);
        
        List<Friend> pendingRequests = friendRepository.findByFriendIdAndStatus(userId, FriendStatus.PENDING);

        List<FriendResponseDto> result = pendingRequests.stream()
                .map(this::mapPendingRequestToDto)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        
        log.info("Retrieved {} pending requests for user: {}", result.size(), userId);
        return result;
    }

    /**
     * Retrieves friend requests sent by a user.
     *
     * @param userId the ID of the user to get sent requests for
     * @return list of sent friend requests
     */
    @Override
    public List<FriendResponseDto> getSentFriendRequests(Long userId) {
        log.info("Retrieving sent friend requests for user: {}", userId);
        
        List<Friend> sentRequests = friendRepository.findByUserIdAndStatus(userId, FriendStatus.PENDING);

        List<FriendResponseDto> result = sentRequests.stream()
                .map(this::mapSentRequestToDto)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        
        log.info("Retrieved {} sent requests for user: {}", result.size(), userId);
        return result;
    }

    // Private helper methods following clean code principle (rule 17: separate complex logic)

    /**
     * Maps User entity to UserSearchResponseDto with relationship status.
     *
     * @param user the user entity to map
     * @param currentUserId the current user's ID for relationship determination
     * @return the mapped DTO
     */
    private UserSearchResponseDto mapToUserSearchResponseDto(User user, Long currentUserId) {
        UserSearchResponseDto dto = new UserSearchResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        dto.setAvatarUrl(user.getAvatarUrl());

        String relationshipStatus = determineRelationshipStatus(currentUserId, user.getId());
        dto.setRelationshipStatus(relationshipStatus);
        dto.setCanSendRequest(FriendConstants.RELATIONSHIP_STATUS_NONE.equals(relationshipStatus));

        return dto;
    }

    /**
     * Validates friend request input parameters.
     *
     * @param fromUserId the sender's user ID
     * @param toUserId the receiver's user ID
     * @throws CustomException if users are the same
     */
    private void validateFriendRequestInput(Long fromUserId, Long toUserId) {
        if (fromUserId.equals(toUserId)) {
            throw new CustomException(StatusCode.CANNOT_ADD_YOURSELF);
        }
    }

    /**
     * Validates that a user exists in the system.
     *
     * @param userId the user ID to validate
     * @throws CustomException if user not found
     */
    private void validateUserExists(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(StatusCode.USER_NOT_FOUND));
    }

    /**
     * Validates that no existing relationship exists between users.
     *
     * @param fromUserId the sender's user ID
     * @param toUserId the receiver's user ID
     * @throws CustomException if relationship already exists
     */
    private void validateNoExistingRelationship(Long fromUserId, Long toUserId) {
        if (friendRepository.existsFriendshipWithStatuses(fromUserId, toUserId,
                Arrays.asList(FriendStatus.PENDING, FriendStatus.ACCEPTED))) {
            throw new CustomException(StatusCode.FRIEND_REQUEST_ALREADY_SENT);
        }
    }

    /**
     * Creates a new friend request entity.
     *
     * @param fromUserId the sender's user ID
     * @param toUserId the receiver's user ID
     * @return the created friend request
     */
    private Friend createFriendRequest(Long fromUserId, Long toUserId) {
        // Use entity references instead of full fetch for better performance
        User fromUser = userRepository.getReferenceById(fromUserId);
        User toUser = userRepository.getReferenceById(toUserId);

        Friend friendRequest = new Friend();
        Friend.FriendId friendId = new Friend.FriendId(fromUserId, toUserId);
        friendRequest.setId(friendId);
        friendRequest.setUser(fromUser);
        friendRequest.setFriend(toUser);
        friendRequest.setStatus(FriendStatus.PENDING);
        return friendRequest;
    }

    /**
     * Finds a pending friend request between users.
     *
     * @param fromUserId the sender's user ID
     * @param userId the receiver's user ID
     * @return the friend request
     * @throws CustomException if friend request not found
     */
    private Friend findPendingFriendRequest(Long fromUserId, Long userId) {
        return friendRepository.findByUserIdAndFriendId(fromUserId, userId)
                .orElseThrow(() -> new CustomException(StatusCode.FRIEND_REQUEST_NOT_FOUND));
    }

    /**
     * Validates that friend request has pending status.
     *
     * @param friendRequest the friend request to validate
     * @throws CustomException if status is not pending
     */
    private void validateFriendRequestStatus(Friend friendRequest) {
        if (friendRequest.getStatus() != FriendStatus.PENDING) {
            throw new CustomException(StatusCode.FRIEND_REQUEST_ALREADY_RESPONDED);
        }
    }

    /**
     * Updates friend request status.
     *
     * @param friendRequest the friend request to update
     * @param status the new status
     */
    private void updateFriendRequestStatus(Friend friendRequest, FriendStatus status) {
        friendRequest.setStatus(status);
        friendRepository.save(friendRequest);
    }

    /**
     * Creates reverse friendship for bidirectional relationship.
     *
     * @param userId the first user's ID
     * @param fromUserId the second user's ID
     */
    private void createReverseFriendship(Long userId, Long fromUserId) {
        // Use entity references instead of full fetch for better performance
        User user = userRepository.getReferenceById(userId);
        User fromUser = userRepository.getReferenceById(fromUserId);

        Friend reverseFriendship = new Friend();
        Friend.FriendId reverseId = new Friend.FriendId(userId, fromUserId);
        reverseFriendship.setId(reverseId);
        reverseFriendship.setUser(user);
        reverseFriendship.setFriend(fromUser);
        reverseFriendship.setStatus(FriendStatus.ACCEPTED);
        friendRepository.save(reverseFriendship);
    }

    /**
     * Maps friendship entity to response DTO.
     *
     * @param friendship the friendship entity
     * @param userId the current user's ID
     * @return the mapped DTO or null if friend user not found
     */
    private FriendResponseDto mapToFriendResponseDto(Friend friendship, Long userId) {
        Long friendId = determineFriendId(friendship, userId);
        User friend = userRepository.findById(friendId).orElse(null);

        if (friend == null) return null;

        return createFriendResponseDto(friend, friendship);
    }

    /**
     * Determines the friend's user ID from friendship entity.
     *
     * @param friendship the friendship entity
     * @param userId the current user's ID
     * @return the friend's user ID
     */
    private Long determineFriendId(Friend friendship, Long userId) {
        return friendship.getId().getUserId().equals(userId)
                ? friendship.getId().getFriendId()
                : friendship.getId().getUserId();
    }

    /**
     * Maps pending friend request to response DTO.
     *
     * @param request the friend request entity
     * @return the mapped DTO or null if sender not found
     */
    private FriendResponseDto mapPendingRequestToDto(Friend request) {
        User sender = userRepository.findById(request.getId().getUserId()).orElse(null);
        return sender != null ? createFriendResponseDto(sender, request) : null;
    }

    /**
     * Maps sent friend request to response DTO.
     *
     * @param request the friend request entity
     * @return the mapped DTO or null if receiver not found
     */
    private FriendResponseDto mapSentRequestToDto(Friend request) {
        User receiver = userRepository.findById(request.getId().getFriendId()).orElse(null);
        return receiver != null ? createFriendResponseDto(receiver, request) : null;
    }

    /**
     * Creates FriendResponseDto from user and friendship data.
     *
     * @param user the user entity
     * @param friendship the friendship entity
     * @return the created DTO
     */
    private FriendResponseDto createFriendResponseDto(User user, Friend friendship) {
        FriendResponseDto dto = new FriendResponseDto();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setStatus(friendship.getStatus());
        dto.setCreatedAt(friendship.getCreatedAt());
        dto.setIsOnline(redisService.isUserOnline(user.getId()));
        return dto;
    }

    /**
     * Determines relationship status between two users.
     *
     * @param userId1 the first user's ID
     * @param userId2 the second user's ID
     * @return the relationship status
     */
    private String determineRelationshipStatus(Long userId1, Long userId2) {
        if (friendRepository.existsFriendshipWithStatuses(userId1, userId2,
                List.of(FriendStatus.ACCEPTED))) {
            return FriendConstants.RELATIONSHIP_STATUS_FRIENDS;
        }

        if (friendRepository.existsFriendshipWithStatuses(userId1, userId2,
                List.of(FriendStatus.PENDING))) {
            return FriendConstants.RELATIONSHIP_STATUS_PENDING;
        }

        if (friendRepository.existsFriendshipWithStatuses(userId1, userId2,
                List.of(FriendStatus.BLOCKED))) {
            return FriendConstants.RELATIONSHIP_STATUS_BLOCKED;
        }

        return FriendConstants.RELATIONSHIP_STATUS_NONE;
    }
}
