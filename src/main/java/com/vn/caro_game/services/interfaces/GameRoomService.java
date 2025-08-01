package com.vn.caro_game.services.interfaces;

import com.vn.caro_game.dtos.request.*;
import com.vn.caro_game.dtos.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Game Room operations.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
public interface GameRoomService {

    /**
     * Creates a new game room.
     * 
     * @param request the create room request data
     * @param userId the ID of the user creating the room
     * @return the created room response
     */
    GameRoomResponse createRoom(CreateRoomRequest request, Long userId);

    /**
     * Joins a room by room ID (for public rooms or rooms user has access to).
     * 
     * @param roomId the room ID to join
     * @param userId the user ID joining the room
     * @return the updated room response
     */
    GameRoomResponse joinRoom(Long roomId, Long userId);

    /**
     * Joins a room using a join code (for private rooms).
     * 
     * @param request the join room request containing join code
     * @param userId the user ID joining the room
     * @return the joined room response
     */
    GameRoomResponse joinRoomByCode(JoinRoomRequest request, Long userId);

    /**
     * Finds or creates a public room for quick play.
     * If no available public room exists, creates a new one.
     * 
     * @param userId the user ID looking for a room
     * @return the room response (existing or newly created)
     */
    GameRoomResponse findOrCreatePublicRoom(Long userId);

    /**
     * Leaves a room.
     * 
     * @param roomId the room ID to leave
     * @param userId the user ID leaving the room
     */
    void leaveRoom(Long roomId, Long userId);

    /**
     * Surrenders the current game.
     * Player who surrenders loses automatically.
     * 
     * @param roomId the room ID
     * @param userId the user ID surrendering
     */
    void surrenderGame(Long roomId, Long userId);

    /**
     * Creates a rematch (new room) with the same players from a finished game.
     * 
     * @param oldRoomId the finished room ID
     * @param userId the user ID requesting the rematch
     * @return the new room response
     */
    GameRoomResponse createRematch(Long oldRoomId, Long userId);

    /**
     * Gets room details by room ID.
     * 
     * @param roomId the room ID
     * @param userId the user ID requesting the details (for access control)
     * @return the room response
     */
    GameRoomResponse getRoomDetails(Long roomId, Long userId);

    /**
     * Gets list of public rooms available for joining.
     * 
     * @param pageable pagination information
     * @return page of public room responses
     */
    Page<PublicRoomResponse> getPublicRooms(Pageable pageable);

    /**
     * Gets rooms where the user is a member.
     * 
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of room responses where user is a member
     */
    Page<GameRoomResponse> getUserRooms(Long userId, Pageable pageable);

    /**
     * Invites a friend to a room.
     * 
     * @param roomId the room ID
     * @param request the invite friend request
     * @param userId the user ID sending the invitation
     */
    void inviteFriend(Long roomId, InviteFriendRequest request, Long userId);

    /**
     * Sends a chat message in a room.
     * 
     * @param roomId the room ID
     * @param request the chat message request
     * @param userId the user ID sending the message
     * @return the sent chat message response
     */
    ChatMessageResponse sendChatMessage(Long roomId, SendChatMessageRequest request, Long userId);

    /**
     * Gets chat messages for a room.
     * 
     * @param roomId the room ID
     * @param userId the user ID requesting the messages (for access control)
     * @param pageable pagination information
     * @return page of chat message responses
     */
    Page<ChatMessageResponse> getRoomChatMessages(Long roomId, Long userId, Pageable pageable);

    /**
     * Gets the latest chat messages for a room.
     * 
     * @param roomId the room ID
     * @param userId the user ID requesting the messages (for access control)
     * @param limit maximum number of messages to return
     * @return list of latest chat message responses
     */
    List<ChatMessageResponse> getLatestChatMessages(Long roomId, Long userId, int limit);

    /**
     * Gets the current active room for a user.
     * 
     * @param userId the user ID
     * @return the active room response if found, null otherwise
     */
    GameRoomResponse getCurrentUserRoom(Long userId);

    /**
     * Marks a player as ready to start the game.
     * Auto-starts game when both players are ready.
     * 
     * @param roomId the room ID
     * @param userId the user ID marking ready
     */
    void markPlayerReady(Long roomId, Long userId);

    /**
     * Requests a rematch (step 1 of 2-step rematch process).
     * 
     * @param roomId the finished room ID
     * @param userId the user ID requesting the rematch
     */
    void requestRematch(Long roomId, Long userId);

    /**
     * Accepts a rematch request (step 2 of 2-step rematch process).
     * Creates new room when both players have accepted.
     * 
     * @param roomId the finished room ID
     * @param userId the user ID accepting the rematch
     */
    void acceptRematch(Long roomId, Long userId);

    /**
     * Gets game history for a user.
     * 
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of game history
     */
    Page<GameHistoryResponse> getUserGameHistory(Long userId, Pageable pageable);

    /**
     * Completes a game with a winner (normal game completion).
     * This is called when someone wins the game normally.
     * 
     * @param roomId the room ID
     * @param winnerId the user ID of the winner
     * @param loserId the user ID of the loser
     */
    void completeGame(Long roomId, Long winnerId, Long loserId);

    /**
     * Starts the game in a room (changes status from WAITING to PLAYING).
     * 
     * @param roomId the room ID
     * @param userId the user ID starting the game (must be host)
     * @return the updated room response
     */
    GameRoomResponse startGame(Long roomId, Long userId);
}
