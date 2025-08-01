package com.vn.caro_game.controllers;

import com.vn.caro_game.integrations.redis.RedisService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Development WebSocket controller for testing STOMP functionality.
 * 
 * <p>This controller is only available in the development profile and provides
 * endpoints for testing WebSocket STOMP communication and message broadcasting.</p>
 * 
 * <h3>Available Endpoints:</h3>
 * <ul>
 *   <li><strong>GET /api/test/websocket</strong> - Health check endpoint</li>
 *   <li><strong>GET /api/test/send-pong</strong> - Send test message to topic</li>
 *   <li><strong>STOMP /ping</strong> - WebSocket ping/pong test</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // HTTP Test
 * curl http://localhost:8080/api/test/websocket
 * 
 * // WebSocket Test (JavaScript)
 * stompClient.send("/app/ping", {}, "Hello Server!");
 * }</pre>
 * 
 * @author Caro Game Team
 * @since 1.0.0
 * @see SimpMessagingTemplate
 */
@Controller
@RequiredArgsConstructor
@Profile("dev")
@Tag(name = "Development WebSocket", description = "WebSocket testing endpoints (Dev only)")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DevWebSocketController {
    
    RedisService redisService;
    long ONLINE_TTL = 300;

    /**
     * Handles WebSocket ping messages and responds with pong.
     * 
     * <p>This STOMP endpoint receives messages sent to "/app/ping" and
     * broadcasts responses to "/topic/pong" for WebSocket testing.
     * Also refreshes the user's online status TTL in Redis.</p>
     * 
     * @param message the ping message received from client
     * @param headerAccessor STOMP header accessor to get session info
     * @return pong response message
     */
    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public String handlePing(String message, StompHeaderAccessor headerAccessor) {
        try {
            // Refresh user online status if session has userId
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

            if (sessionAttributes != null) {
                Long userId = (Long) sessionAttributes.get("userId");

                if (userId != null) {
                    // Refresh online TTL for this user
                    redisService.refreshUserOnline(userId, ONLINE_TTL);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "PONG: " + message + " (received at " + System.currentTimeMillis() + ")";
    }
}