package com.vn.caro_game.dtos.websocket;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * WebSocket message for room updates.
 * Used for broadcasting room status changes and events.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "WebSocket message for room updates")
public class RoomUpdateMessage {
    
    @Schema(description = "Room ID", example = "1")
    Long roomId;
    
    @Schema(description = "Type of update", example = "PLAYER_JOINED")
    String updateType;
    
    @Schema(description = "Timestamp of update", example = "1684567890123")
    long timestamp;

    public RoomUpdateMessage(Long roomId, String updateType) {
        this.roomId = roomId;
        this.updateType = updateType;
        this.timestamp = System.currentTimeMillis();
    }
}
