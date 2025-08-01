package com.vn.caro_game.dtos.response;

import com.vn.caro_game.enums.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Response DTO for public room list.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Public room summary information")
public class PublicRoomResponse {

    @Schema(description = "Room ID", example = "1")
    Long id;

    @Schema(description = "Room name", example = "Epic Caro Battle")
    String name;

    @Schema(description = "Room status", example = "WAITING")
    RoomStatus status;

    @Schema(description = "Room creator name", example = "John Doe")
    String createdByName;

    @Schema(description = "Room creation time", example = "2024-01-15T10:30:00")
    LocalDateTime createdAt;

    @Schema(description = "Current player count", example = "1")
    Integer currentPlayerCount;

    @Schema(description = "Maximum players allowed", example = "2")
    Integer maxPlayers;

    @Schema(description = "Whether room is joinable", example = "true")
    Boolean isJoinable;
}
