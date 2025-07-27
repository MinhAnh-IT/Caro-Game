package com.vn.caro_game.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import lombok.RequiredArgsConstructor;

/**
 * WebSocket configuration for STOMP messaging protocol.
 * 
 * <p>This configuration enables WebSocket communication with STOMP protocol support,
 * providing real-time bidirectional communication capabilities for the Caro Game
 * application. It includes JWT token authentication through handshake interceptors.</p>
 * 
 * <h3>Message Broker Configuration:</h3>
 * <ul>
 *   <li><strong>/topic</strong> - Broadcast destinations for public messages</li>
 *   <li><strong>/queue</strong> - Point-to-point messaging for private messages</li>
 *   <li><strong>/app</strong> - Application destination prefix for client-to-server messages</li>
 * </ul>
 * 
 * <h3>STOMP Endpoints:</h3>
 * <ul>
 *   <li><strong>/ws</strong> - Native WebSocket endpoint with JWT authentication</li>
 *   <li><strong>/ws-sockjs</strong> - SockJS fallback endpoint for older browsers</li>
 * </ul>
 * 
 * <h3>Security:</h3>
 * <p>All endpoints are protected by {@link JwtHandshakeInterceptor} which validates
 * JWT tokens passed as query parameters during the WebSocket handshake process.</p>
 * 
 * @author Caro Game Team
 * @since 1.0.0
 * @see JwtHandshakeInterceptor
 * @see WebSocketEventListener
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint cho STOMP over WebSocket thuần (không dùng SockJS)
        registry.addEndpoint("/ws")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
                
        // Endpoint cho SockJS (fallback)
        registry.addEndpoint("/ws-sockjs")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
