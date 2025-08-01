package com.vn.caro_game.controllers;

import com.vn.caro_game.configs.CustomUserDetails;
import com.vn.caro_game.dtos.request.GameCompleteRequest;
import com.vn.caro_game.dtos.request.SendChatMessageRequest;
import com.vn.caro_game.dtos.response.ChatMessageResponse;
import com.vn.caro_game.services.interfaces.GameRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/**
 * WebSocket Controller for real-time Game Room features.
 * Handles WebSocket messages for chat and room updates.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class GameRoomWebSocketController {

    private final GameRoomService gameRoomService;

    /**
     * Handle chat messages sent via WebSocket
     * Messages are sent to: /app/room/{roomId}/chat
     * Responses are broadcast to: /topic/room/{roomId}/chat
     * 
     * @param roomId the room ID
     * @param request the chat message request
     * @param authentication the user authentication
     */
    @MessageMapping("/room/{roomId}/chat")
    public void handleChatMessage(
            @DestinationVariable Long roomId,
            @Payload SendChatMessageRequest request,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.debug("Received WebSocket chat message for room {} from user {}", roomId, userDetails.getUserId());
            
            // Send chat message (this will also broadcast via WebSocket)
            ChatMessageResponse response = gameRoomService.sendChatMessage(roomId, request, userDetails.getUserId());
            
            log.debug("Successfully processed WebSocket chat message: {}", response.getId());
        } catch (Exception e) {
            log.error("Error processing WebSocket chat message for room {}: {}", roomId, e.getMessage(), e);
            // Note: Error handling for WebSocket is different from REST
            // We could send error messages back to the specific user if needed
        }
    }

    /**
     * Handle join room requests via WebSocket
     * Messages are sent to: /app/room/{roomId}/join
     * Success responses are broadcast to: /topic/room/{roomId}/updates
     * 
     * @param roomId the room ID to join
     * @param authentication the user authentication
     */
    @MessageMapping("/room/{roomId}/join")
    public void handleJoinRoom(
            @DestinationVariable Long roomId,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.debug("Received WebSocket join request for room {} from user {}", roomId, userDetails.getUserId());
            
            // Join room (this will also broadcast room updates via WebSocket)
            gameRoomService.joinRoom(roomId, userDetails.getUserId());
            
            log.debug("Successfully processed WebSocket join for room {}: user {} joined", roomId, userDetails.getUserId());
        } catch (Exception e) {
            log.error("Error processing WebSocket join for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    /**
     * Handle leave room requests via WebSocket
     * Messages are sent to: /app/room/{roomId}/leave
     * Success responses are broadcast to: /topic/room/{roomId}/updates
     * 
     * @param roomId the room ID to leave
     * @param authentication the user authentication
     */
    @MessageMapping("/room/{roomId}/leave")
    public void handleLeaveRoom(
            @DestinationVariable Long roomId,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.debug("Received WebSocket leave request for room {} from user {}", roomId, userDetails.getUserId());
            
            // Leave room (this will also broadcast room updates via WebSocket)
            gameRoomService.leaveRoom(roomId, userDetails.getUserId());
            
            log.debug("Successfully processed WebSocket leave for room {}: user {} left", roomId, userDetails.getUserId());
        } catch (Exception e) {
            log.error("Error processing WebSocket leave for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    /**
     * Handle ready to start requests via WebSocket
     * Messages are sent to: /app/room/{roomId}/ready
     * Success responses are broadcast to: /topic/room/{roomId}/updates
     * 
     * @param roomId the room ID
     * @param authentication the user authentication
     */
    @MessageMapping("/room/{roomId}/ready")
    public void handlePlayerReady(
            @DestinationVariable Long roomId,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.debug("Received WebSocket ready request for room {} from user {}", roomId, userDetails.getUserId());
            
            // Mark player as ready (backend will check if both ready and auto-start)
            gameRoomService.markPlayerReady(roomId, userDetails.getUserId());
            
            log.debug("Successfully processed WebSocket ready for room {}: user {} marked ready", roomId, userDetails.getUserId());
        } catch (Exception e) {
            log.error("Error processing WebSocket ready for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    /**
     * Handle start game requests via WebSocket
     * Messages are sent to: /app/room/{roomId}/start
     * Success responses are broadcast to: /topic/room/{roomId}/updates
     * 
     * @param roomId the room ID to start game
     * @param authentication the user authentication
     * @deprecated Use /ready endpoint instead for new ready system
     */
    @MessageMapping("/room/{roomId}/start")
    public void handleStartGame(
            @DestinationVariable Long roomId,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.debug("Received WebSocket start game request for room {} from user {}", roomId, userDetails.getUserId());
            
            // Legacy start game - should migrate to ready system
            gameRoomService.startGame(roomId, userDetails.getUserId());
            
            log.debug("Successfully processed WebSocket start game for room {}: game started", roomId);
        } catch (Exception e) {
            log.error("Error processing WebSocket start game for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    /**
     * Handle room status requests via WebSocket
     * Messages are sent to: /app/room/{roomId}/status
     * Responses are sent back to the requesting user
     * 
     * @param roomId the room ID to get status for
     * @param authentication the user authentication
     */
    @MessageMapping("/room/{roomId}/status")
    public void handleRoomStatus(
            @DestinationVariable Long roomId,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.debug("Received WebSocket room status request for room {} from user {}", roomId, userDetails.getUserId());
            
            // Get room details
            gameRoomService.getRoomDetails(roomId, userDetails.getUserId());
            
            // This could be enhanced to send the response back to the specific user
            // For now, the service layer handles broadcasting room updates
            
            log.debug("Successfully processed WebSocket room status for room {}", roomId);
        } catch (Exception e) {
            log.error("Error processing WebSocket room status for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    /**
     * Handle surrender requests via WebSocket
     * Messages are sent to: /app/room/{roomId}/surrender
     * Player surrenders and automatically loses the game
     * 
     * @param roomId the room ID to surrender in
     * @param authentication the user authentication
     */
    @MessageMapping("/room/{roomId}/surrender")
    public void handleSurrender(
            @DestinationVariable Long roomId,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.debug("Received WebSocket surrender request for room {} from user {}", roomId, userDetails.getUserId());
            
            // Surrender game (this will also broadcast game result via WebSocket)
            gameRoomService.surrenderGame(roomId, userDetails.getUserId());
            
            log.debug("Successfully processed WebSocket surrender for room {}: game ended", roomId);
        } catch (Exception e) {
            log.error("Error processing WebSocket surrender for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    /**
     * Handle rematch request via WebSocket (step 1 of 2)
     * Messages are sent to: /app/room/{roomId}/rematch/request
     * Responses are broadcast to: /topic/room/{roomId}/updates
     * 
     * @param roomId the room ID
     * @param authentication the user authentication
     */
    @MessageMapping("/room/{roomId}/rematch/request")
    public void handleRematchRequest(
            @DestinationVariable Long roomId,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.debug("Received WebSocket rematch request for room {} from user {}", roomId, userDetails.getUserId());
            
            // Request rematch - other player needs to accept
            gameRoomService.requestRematch(roomId, userDetails.getUserId());
            
            log.debug("Successfully processed WebSocket rematch request for room {}", roomId);
        } catch (Exception e) {
            log.error("Error processing WebSocket rematch request for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    /**
     * Handle rematch accept via WebSocket (step 2 of 2)
     * Messages are sent to: /app/room/{roomId}/rematch/accept
     * Responses are broadcast to: /topic/room/{roomId}/updates
     * 
     * @param roomId the room ID
     * @param authentication the user authentication
     */
    @MessageMapping("/room/{roomId}/rematch/accept")
    public void handleRematchAccept(
            @DestinationVariable Long roomId,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.debug("Received WebSocket rematch accept for room {} from user {}", roomId, userDetails.getUserId());
            
            // Accept rematch - create new room if both accepted
            gameRoomService.acceptRematch(roomId, userDetails.getUserId());
            
            log.debug("Successfully processed WebSocket rematch accept for room {}", roomId);
        } catch (Exception e) {
            log.error("Error processing WebSocket rematch accept for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    /**
     * Handle rematch requests sent via WebSocket (legacy - single step)
     * Messages are sent to: /app/room/{roomId}/rematch
     * Responses are broadcast to: /topic/room/{roomId}/updates
     * 
     * @param roomId the room ID
     * @param authentication the user authentication
     * @deprecated Use /rematch/request and /rematch/accept for better UX
     */
    @MessageMapping("/room/{roomId}/rematch")
    public void handleRematch(
            @DestinationVariable Long roomId,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.debug("Received WebSocket rematch request for room {} from user {}", roomId, userDetails.getUserId());
            
            // Legacy create rematch - immediate room creation
            gameRoomService.createRematch(roomId, userDetails.getUserId());
            
            log.debug("Successfully processed WebSocket rematch for room {}: new room created", roomId);
        } catch (Exception e) {
            log.error("Error processing WebSocket rematch for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    /**
     * Handle game completion sent via WebSocket
     * Messages are sent to: /app/room/{roomId}/complete
     * 
     * @param roomId the room ID
     * @param gameResult contains winner and loser IDs
     * @param authentication the user authentication
     */
    @MessageMapping("/room/{roomId}/complete")
    public void handleGameComplete(
            @DestinationVariable Long roomId,
            @Payload GameCompleteRequest gameResult,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.debug("Received WebSocket game completion for room {} from user {}", roomId, userDetails.getUserId());
            
            // Complete the game with winner and loser
            gameRoomService.completeGame(roomId, gameResult.getWinnerId(), gameResult.getLoserId());
            
            log.debug("Successfully processed WebSocket game completion for room {}", roomId);
        } catch (Exception e) {
            log.error("Error processing WebSocket game completion for room {}: {}", roomId, e.getMessage(), e);
        }
    }
}
