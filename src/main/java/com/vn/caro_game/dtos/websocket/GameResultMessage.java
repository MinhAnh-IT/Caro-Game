package com.vn.caro_game.dtos.websocket;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * WebSocket message for game results.
 * Used when broadcasting game end results and winner information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "WebSocket message for game results")
public class GameResultMessage {
    
    @Schema(description = "Room ID", example = "1")
    Long roomId;
    
    @Schema(description = "Winner user ID", example = "123")
    Long winnerId;
    
    @Schema(description = "Loser user ID", example = "456")
    Long loserId;
    
    @Schema(description = "Game end reason", example = "CHECKMATE")
    String reason;
    
    @Schema(description = "Result timestamp", example = "1684567890123")
    long timestamp;
}
