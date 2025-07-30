package com.vn.caro_game.configs;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * JWT Token Handshake Interceptor for WebSocket connections.
 * 
 * <p>This interceptor extracts JWT tokens from WebSocket connection query parameters
 * and stores them in the WebSocket session attributes for later use by event listeners
 * and message handlers.</p>
 * 
 * <h3>Token Extraction Process:</h3>
 * <ol>
 *   <li>Parses the WebSocket connection URI for 'token' query parameter</li>
 *   <li>Performs URL decoding to handle encoded token values</li>
 *   <li>Removes 'Bearer ' prefix if present</li>
 *   <li>Stores the clean token in session attributes with key 'jwt'</li>
 * </ol>
 * 
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // WebSocket connection URL
 * ws://localhost:8080/ws?token=Bearer%20eyJhbGciOiJIUzI1NiJ9...
 * 
 * // After interception, session attributes will contain:
 * attributes.get("jwt") // -> "eyJhbGciOiJIUzI1NiJ9..."
 * }</pre>
 * 
 * @author Caro Game Team
 * @since 1.0.0
 * @see WebSocketEventListener
 * @see WebSocketConfig
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            // Lấy token từ query string
            String uri = request.getURI().toString();

            String token = null;
            if (uri.contains("token=")) {
                String[] parts = uri.split("token=");
                if (parts.length > 1) {
                    token = parts[1];
                    int amp = token.indexOf('&');
                    if (amp != -1) token = token.substring(0, amp);
                    
                    // URL decode token
                    token = URLDecoder.decode(token, StandardCharsets.UTF_8);
                    
                    if (token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    }
                    attributes.put("jwt", token);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception exception) {
        // No-op
    }
}
