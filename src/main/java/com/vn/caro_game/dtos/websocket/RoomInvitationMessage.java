package com.vn.caro_game.dtos.websocket;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * WebSocket message for room invitations.
 * Used when sending room invitations to friends via WebSocket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "WebSocket message for room invitations")
public class RoomInvitationMessage {
    
    @Schema(description = "Room ID", example = "1")
    Long roomId;
    
    @Schema(description = "Room name", example = "My Game Room")
    String roomName;
    
    @Schema(description = "Name of the user sending invitation", example = "Player1")
    String inviterName;
    
    @Schema(description = "Join code for private rooms", example = "ABC1")
    String joinCode;
    
    @Schema(description = "Invitation message", example = "Join my game!")
    String message;
}
