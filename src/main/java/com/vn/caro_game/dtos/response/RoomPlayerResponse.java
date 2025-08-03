package com.vn.caro_game.dtos.response;

import com.vn.caro_game.enums.GameResult;
import com.vn.caro_game.enums.PlayerReadyState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Response DTO for room player information.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Room player information")
public class RoomPlayerResponse {

    @Schema(description = "Player information")
    UserSummaryResponse player;

    @Schema(description = "Whether player is room host", example = "true")
    Boolean isHost;

    @Schema(description = "Time when player joined the room", example = "2024-01-15T10:30:00")
    LocalDateTime joinTime;

    @Schema(description = "Whether player is online", example = "true")
    Boolean isOnline;

    @Schema(description = "Player ready state", example = "NOT_READY")
    PlayerReadyState readyState;

    @Schema(description = "Game result for this player", example = "NONE")
    GameResult gameResult;

    @Schema(description = "Whether player accepted rematch", example = "false")
    Boolean acceptedRematch;

    // Legacy constructor for backward compatibility
    public RoomPlayerResponse(UserSummaryResponse player, Boolean isHost, LocalDateTime joinTime, Boolean isOnline) {
        this.player = player;
        this.isHost = isHost;
        this.joinTime = joinTime;
        this.isOnline = isOnline;
        
        // Set default values for new fields
        this.readyState = PlayerReadyState.NOT_READY;
        this.gameResult = GameResult.NONE;
        this.acceptedRematch = false;
    }

    // Full constructor for enhanced features
    public RoomPlayerResponse(UserSummaryResponse player, Boolean isHost, LocalDateTime joinTime, Boolean isOnline,
                             PlayerReadyState readyState, GameResult gameResult, Boolean acceptedRematch) {
        this.player = player;
        this.isHost = isHost;
        this.joinTime = joinTime;
        this.isOnline = isOnline;
        this.readyState = readyState;
        this.gameResult = gameResult;
        this.acceptedRematch = acceptedRematch;
    }
}
