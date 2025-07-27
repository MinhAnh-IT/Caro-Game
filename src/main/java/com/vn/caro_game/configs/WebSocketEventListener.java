package com.vn.caro_game.configs;

import com.vn.caro_game.integrations.redis.RedisService;
import com.vn.caro_game.integrations.redis.RedisPublisher;
import com.vn.caro_game.integrations.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

/**
 * WebSocket Event Listener for handling user online status.
 * 
 * <p>This listener responds to WebSocket STOMP connection and disconnection events
 * to automatically manage user online status in Redis cache. It integrates with
 * JWT authentication to identify users and maintain their online presence.</p>
 * 
 * <h3>Features:</h3>
 * <ul>
 *   <li>Automatic online status management on WebSocket connect/disconnect</li>
 *   <li>JWT token validation for secure user identification</li>
 *   <li>Redis TTL-based online status with 5-minute timeout</li>
 *   <li>Real-time status broadcasting via Redis Pub/Sub</li>
 * </ul>
 * 
 * <h3>Flow:</h3>
 * <ol>
 *   <li>User connects to WebSocket with JWT token in query parameters</li>
 *   <li>JwtHandshakeInterceptor extracts and validates token</li>
 *   <li>This listener handles SessionConnectEvent and sets user online in Redis</li>
 *   <li>On disconnect, user remains online until Redis TTL expires</li>
 * </ol>
 * 
 * @author Caro Game Team
 * @since 1.0.0
 * @see JwtHandshakeInterceptor
 * @see RedisService
 * @see RedisPublisher
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {
    
    private final JwtService jwtService;
    private final RedisService redisService;
    private final RedisPublisher redisPublisher;
    private static final long ONLINE_TTL = 300;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            
            // Kiểm tra session attributes không null
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
            if (sessionAttributes == null) {
                log.warn("=== WEBSOCKET CONNECT WITH NULL SESSION ATTRIBUTES ===");
                return;
            }
            
            // Lấy JWT token từ session attributes (đã được set bởi JwtHandshakeInterceptor)
            String token = (String) sessionAttributes.get("jwt");
            
            if (token != null && jwtService.validateToken(token)) {
                Long userId = jwtService.getUserIdFromToken(token);
                
                // Lưu userId vào session để dùng khi disconnect
                sessionAttributes.put("userId", userId);
                
                // Set user online trong Redis
                redisService.setUserOnline(userId, ONLINE_TTL);
                redisPublisher.publishUserStatus(userId, "online");
                
                log.info("=== USER {} CONNECTED TO WEBSOCKET - SET ONLINE ===", userId);
            } else {
                log.warn("=== WEBSOCKET CONNECT WITHOUT VALID TOKEN ===");
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket connect event", e);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
            if (sessionAttributes != null) {
                Long userId = (Long) sessionAttributes.get("userId");
                
                if (userId != null) {
                    // Không xóa ngay, để Redis TTL tự xử lý sau 70s
                    log.info("=== USER {} DISCONNECTED FROM WEBSOCKET ===", userId);
                }
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket disconnect event", e);
        }
    }
}
