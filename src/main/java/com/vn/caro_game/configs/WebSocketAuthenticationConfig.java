package com.vn.caro_game.configs;

import com.vn.caro_game.integrations.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Authentication Configuration.
 * 
 * <p>This configuration sets up authentication for WebSocket STOMP messages by
 * intercepting incoming messages and validating JWT tokens from the session attributes
 * set during the handshake process.</p>
 * 
 * <h3>Flow:</h3>
 * <ol>
 *   <li>JWT token is extracted during handshake by JwtHandshakeInterceptor</li>
 *   <li>This interceptor validates the token for each STOMP message</li>
 *   <li>Sets up Spring Security authentication context</li>
 *   <li>Enables @MessageMapping methods to access Authentication parameter</li>
 * </ol>
 * 
 * @author Caro Game Team
 * @since 1.0.0
 * @see JwtHandshakeInterceptor
 * @see WebSocketEventListener
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthenticationConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // For CONNECT command, authentication is handled by WebSocketEventListener
                    return message;
                }
                
                if (accessor != null && accessor.getSessionAttributes() != null) {
                    String token = (String) accessor.getSessionAttributes().get("jwt");
                    
                    if (token != null) {
                        try {
                            if (jwtService.validateToken(token)) {
                                String email = jwtService.extractEmail(token);
                                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                                
                                UsernamePasswordAuthenticationToken authToken = 
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                                
                                // Set authentication in both SecurityContext and StompHeaderAccessor
                                SecurityContextHolder.getContext().setAuthentication(authToken);
                                accessor.setUser(authToken);
                                
                                log.debug("WebSocket message authenticated for user: {}", email);
                            } else {
                                log.warn("Invalid JWT token in WebSocket message");
                            }
                        } catch (Exception e) {
                            log.error("Error authenticating WebSocket message: {}", e.getMessage());
                        }
                    } else {
                        log.warn("No JWT token found in WebSocket session attributes");
                    }
                }
                
                return message;
            }
        });
    }
}
