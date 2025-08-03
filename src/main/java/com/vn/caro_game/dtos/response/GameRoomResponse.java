package com.vn.caro_game.dtos.response;

import com.vn.caro_game.enums.GameState;
import com.vn.caro_game.enums.RematchState;
import com.vn.caro_game.enums.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for game room information.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Game room information")
public class GameRoomResponse {

    @Schema(description = "Room ID", example = "1")
    Long id;

    @Schema(description = "Room name", example = "Epic Caro Battle")
    String name;

    @Schema(description = "Room status", example = "WAITING")
    RoomStatus status;

    @Schema(description = "Game state", example = "WAITING_FOR_PLAYERS")
    GameState gameState;

    @Schema(description = "Rematch state", example = "NONE")
    RematchState rematchState;

    @Schema(description = "ID of user who requested rematch")
    Long rematchRequesterId;

    @Schema(description = "ID of new room created for rematch")
    Long newRoomId;

    @Schema(description = "Whether room is private", example = "false")
    Boolean isPrivate;

    @Schema(description = "Join code for private rooms", example = "AB12")
    String joinCode;

    @Schema(description = "Room creator information")
    UserSummaryResponse createdBy;

    @Schema(description = "Room creation time", example = "2024-01-15T10:30:00")
    LocalDateTime createdAt;

    @Schema(description = "Game start time", example = "2024-01-15T10:35:00")
    LocalDateTime gameStartedAt;

    @Schema(description = "Game end time", example = "2024-01-15T10:45:00")
    LocalDateTime gameEndedAt;

    @Schema(description = "List of players in the room")
    List<RoomPlayerResponse> players;

    @Schema(description = "Current player count", example = "2")
    Integer currentPlayerCount;

    @Schema(description = "Maximum players allowed", example = "2")
    Integer maxPlayers;

    @Schema(description = "Number of ready players", example = "1")
    Long readyPlayersCount;

    @Schema(description = "Whether both players are ready", example = "false")
    Boolean bothPlayersReady;

    @Schema(description = "Whether game can be started", example = "false")
    Boolean canStartGame;

    @Schema(description = "Whether rematch can be requested", example = "false")
    Boolean canRematch;

    @Schema(description = "Whether game is currently active", example = "false")
    Boolean isGameActive;

    @Schema(description = "Whether players can mark ready", example = "true")
    Boolean canMarkReady;

    // Legacy constructor for backward compatibility
    public GameRoomResponse(Long id, String name, RoomStatus status, Boolean isPrivate, String joinCode,
                           UserSummaryResponse createdBy, LocalDateTime createdAt, List<RoomPlayerResponse> players,
                           Integer currentPlayerCount, Integer maxPlayers) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.isPrivate = isPrivate;
        this.joinCode = joinCode;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.players = players;
        this.currentPlayerCount = currentPlayerCount;
        this.maxPlayers = maxPlayers;
        
        // Set default values for new fields
        this.gameState = GameState.WAITING_FOR_PLAYERS;
        this.rematchState = RematchState.NONE;
        this.readyPlayersCount = 0L;
        this.bothPlayersReady = false;
        this.canStartGame = false;
        this.canRematch = false;
        this.isGameActive = false;
        this.canMarkReady = currentPlayerCount >= maxPlayers;
    }

    // Full constructor for enhanced features
    public GameRoomResponse(Long id, String name, RoomStatus status, GameState gameState, RematchState rematchState,
                           Long rematchRequesterId, Long newRoomId, Boolean isPrivate, String joinCode,
                           UserSummaryResponse createdBy, LocalDateTime createdAt, LocalDateTime gameStartedAt,
                           LocalDateTime gameEndedAt, List<RoomPlayerResponse> players, Integer currentPlayerCount,
                           Integer maxPlayers, Long readyPlayersCount, Boolean bothPlayersReady, Boolean canStartGame,
                           Boolean canRematch, Boolean isGameActive, Boolean canMarkReady) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.gameState = gameState;
        this.rematchState = rematchState;
        this.rematchRequesterId = rematchRequesterId;
        this.newRoomId = newRoomId;
        this.isPrivate = isPrivate;
        this.joinCode = joinCode;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.gameStartedAt = gameStartedAt;
        this.gameEndedAt = gameEndedAt;
        this.players = players;
        this.currentPlayerCount = currentPlayerCount;
        this.maxPlayers = maxPlayers;
        this.readyPlayersCount = readyPlayersCount;
        this.bothPlayersReady = bothPlayersReady;
        this.canStartGame = canStartGame;
        this.canRematch = canRematch;
        this.isGameActive = isGameActive;
        this.canMarkReady = canMarkReady;
    }
}
