package com.vn.caro_game.mappers;

import com.vn.caro_game.constants.GameRoomConstants;
import com.vn.caro_game.dtos.response.*;
import com.vn.caro_game.entities.ChatMessage;
import com.vn.caro_game.entities.GameRoom;
import com.vn.caro_game.entities.RoomPlayer;
import com.vn.caro_game.entities.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for GameRoom related entities and DTOs.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Component
public class GameRoomMapper {

    /**
     * Maps User entity to UserSummaryResponse DTO.
     * 
     * @param user the user entity
     * @return UserSummaryResponse DTO
     */
    public UserSummaryResponse mapToUserSummaryResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryResponse(
            user.getId(),
            user.getDisplayName(),
            user.getAvatarUrl(),
            user.getUsername()
        );
    }

    /**
     * Maps RoomPlayer entity to RoomPlayerResponse DTO.
     * 
     * @param roomPlayer the room player entity
     * @param isOnline whether the player is currently online
     * @return RoomPlayerResponse DTO
     */
    public RoomPlayerResponse mapToRoomPlayerResponse(RoomPlayer roomPlayer, boolean isOnline) {
        if (roomPlayer == null) {
            return null;
        }
        
        // Use full constructor with enhanced features
        return new RoomPlayerResponse(
            mapToUserSummaryResponse(roomPlayer.getUser()),
            roomPlayer.getIsHost(),
            roomPlayer.getJoinTime(),
            isOnline,
            roomPlayer.getReadyState(),
            roomPlayer.getGameResult(),
            roomPlayer.getAcceptedRematch()
        );
    }

    /**
     * Maps GameRoom entity to GameRoomResponse DTO.
     * 
     * @param gameRoom the game room entity
     * @param players list of room players with online status
     * @return GameRoomResponse DTO
     */
    public GameRoomResponse mapToGameRoomResponse(GameRoom gameRoom, List<RoomPlayerResponse> players) {
        if (gameRoom == null) {
            return null;
        }
        
        // Use full constructor with enhanced features
        return new GameRoomResponse(
            gameRoom.getId(),
            gameRoom.getName(),
            gameRoom.getStatus(),
            gameRoom.getGameState(),
            gameRoom.getRematchState(),
            gameRoom.getRematchRequesterId(),
            gameRoom.getNewRoomId(),
            gameRoom.getIsPrivate(),
            gameRoom.getJoinCode(),
            mapToUserSummaryResponse(gameRoom.getCreatedBy()),
            gameRoom.getCreatedAt(),
            gameRoom.getGameStartedAt(),
            gameRoom.getGameEndedAt(),
            players,
            players != null ? players.size() : 0,
            GameRoomConstants.MAX_PLAYERS_PER_ROOM,
            gameRoom.getReadyPlayersCount(),
            gameRoom.bothPlayersReady(),
            gameRoom.canStartGame(),
            gameRoom.canRematch(),
            gameRoom.isGameActive(),
            // canMarkReady: true if room has enough players AND game is waiting for ready state
            (players != null ? players.size() : 0) >= 2 && 
            gameRoom.getGameState() == com.vn.caro_game.enums.GameState.WAITING_FOR_READY
        );
    }

    /**
     * Maps GameRoom entity to PublicRoomResponse DTO.
     * 
     * @param gameRoom the game room entity
     * @param currentPlayerCount current number of players in the room
     * @return PublicRoomResponse DTO
     */
    public PublicRoomResponse mapToPublicRoomResponse(GameRoom gameRoom, Integer currentPlayerCount) {
        if (gameRoom == null) {
            return null;
        }
        boolean isJoinable = gameRoom.getStatus().equals(com.vn.caro_game.enums.RoomStatus.WAITING) 
                            && currentPlayerCount < GameRoomConstants.MAX_PLAYERS_PER_ROOM;
        
        return new PublicRoomResponse(
            gameRoom.getId(),
            gameRoom.getName(),
            gameRoom.getStatus(),
            gameRoom.getCreatedBy().getDisplayName(),
            gameRoom.getCreatedAt(),
            currentPlayerCount,
            GameRoomConstants.MAX_PLAYERS_PER_ROOM,
            isJoinable
        );
    }

    /**
     * Maps ChatMessage entity to ChatMessageResponse DTO.
     * 
     * @param chatMessage the chat message entity
     * @return ChatMessageResponse DTO
     */
    public ChatMessageResponse mapToChatMessageResponse(ChatMessage chatMessage) {
        if (chatMessage == null) {
            return null;
        }
        return new ChatMessageResponse(
            chatMessage.getId(),
            mapToUserSummaryResponse(chatMessage.getSender()),
            chatMessage.getContent(),
            chatMessage.getSentAt()
        );
    }

    /**
     * Maps list of ChatMessage entities to list of ChatMessageResponse DTOs.
     * 
     * @param chatMessages list of chat message entities
     * @return list of ChatMessageResponse DTOs
     */
    public List<ChatMessageResponse> mapToChatMessageResponseList(List<ChatMessage> chatMessages) {
        if (chatMessages == null) {
            return null;
        }
        return chatMessages.stream()
                .map(this::mapToChatMessageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps list of GameRoom entities to list of PublicRoomResponse DTOs.
     * 
     * @param gameRooms list of game room entities
     * @param playerCounts list of player counts corresponding to each room
     * @return list of PublicRoomResponse DTOs
     */
    public List<PublicRoomResponse> mapToPublicRoomResponseList(List<GameRoom> gameRooms, List<Integer> playerCounts) {
        if (gameRooms == null || playerCounts == null || gameRooms.size() != playerCounts.size()) {
            return null;
        }
        return gameRooms.stream()
                .map(room -> {
                    int index = gameRooms.indexOf(room);
                    return mapToPublicRoomResponse(room, playerCounts.get(index));
                })
                .collect(Collectors.toList());
    }
}
