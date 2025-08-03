package com.vn.caro_game.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.caro_game.configs.CustomUserDetails;
import com.vn.caro_game.dtos.request.GameMoveRequest;
import com.vn.caro_game.dtos.response.GameMoveResponse;
import com.vn.caro_game.services.interfaces.CaroGameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/**
 * WebSocket Controller for real-time Caro Game features.
 * Handles WebSocket messages for game moves and board updates.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class CaroGameWebSocketController {

    private final CaroGameService caroGameService;

    /**
     * Handle game moves sent via WebSocket
     * Messages are sent to: /app/game/{roomId}/move
     * Responses are broadcast to: /topic/game/{roomId}/move
     * 
     * @param roomId the room ID where the game is being played
     * @param request the game move request containing coordinates
     * @param authentication the user authentication
     */
    @MessageMapping("/game/{roomId}/move")
    public void handleGameMove(
            @DestinationVariable Long roomId,
            @Payload String payload,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.debug("Raw WebSocket payload for room {}: {}", roomId, payload);
            
            // Parse JSON manually to avoid null issues
            ObjectMapper objectMapper = new ObjectMapper();
            GameMoveRequest request = objectMapper.readValue(payload, GameMoveRequest.class);
            
            log.debug("Parsed GameMoveRequest: xPosition={}, yPosition={}", 
                    request.getXPosition(), request.getYPosition());
            
            // Process the move (this will also broadcast via WebSocket)
            GameMoveResponse response = caroGameService.makeMove(roomId, request, userDetails.getUserId());
            
            log.debug("Successfully processed WebSocket game move: room={}, move={}, result={}", 
                    roomId, response.getMoveNumber(), response.getMessage());
        } catch (Exception e) {
            log.error("Error processing WebSocket game move for room {}: {}", roomId, e.getMessage(), e);
            // Note: Error handling for WebSocket is different from REST
            // We could send error messages back to the specific user if needed
        }
    }

    /**
     * Handle requests to get current board state via WebSocket
     * Messages are sent to: /app/game/{roomId}/board
     * Responses are sent back to the specific user
     * 
     * @param roomId the room ID to get board state for
     * @param authentication the user authentication
     */
    @MessageMapping("/game/{roomId}/board")
    public void handleGetBoard(
            @DestinationVariable Long roomId,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.debug("Received WebSocket board request for room {} from user {}", 
                    roomId, userDetails.getUserId());
            
            // Get current board state
            // Note: In a full implementation, you might want to send this back to the specific user
            // For now, we'll just log that the request was processed
            log.debug("Successfully processed WebSocket board request for room {}", roomId);
        } catch (Exception e) {
            log.error("Error processing WebSocket board request for room {}: {}", roomId, e.getMessage(), e);
        }
    }
}
