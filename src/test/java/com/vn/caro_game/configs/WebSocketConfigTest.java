package com.vn.caro_game.configs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WebSocketConfig.
 * Tests WebSocket and STOMP messaging configuration.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {

    @Mock
    private JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Mock
    private MessageBrokerRegistry messageBrokerRegistry;

    @Mock
    private StompEndpointRegistry stompEndpointRegistry;

    @InjectMocks
    private WebSocketConfig webSocketConfig;

    @Test
    void configureMessageBroker_ShouldEnableSimpleBroker() {
        // When
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        // Then
        verify(messageBrokerRegistry).enableSimpleBroker("/topic", "/queue");
    }

    @Test
    void configureMessageBroker_ShouldSetApplicationDestinationPrefix() {
        // When
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        // Then
        verify(messageBrokerRegistry).setApplicationDestinationPrefixes("/app");
    }

    @Test
    void registerStompEndpoints_ShouldRegisterBothEndpoints() {
        // Given
        StompWebSocketEndpointRegistration wsEndpoint = mock(StompWebSocketEndpointRegistration.class);
        StompWebSocketEndpointRegistration sockjsEndpoint = mock(StompWebSocketEndpointRegistration.class);
        
        when(stompEndpointRegistry.addEndpoint("/ws")).thenReturn(wsEndpoint);
        when(stompEndpointRegistry.addEndpoint("/ws-sockjs")).thenReturn(sockjsEndpoint);
        
        when(wsEndpoint.addInterceptors(any())).thenReturn(wsEndpoint);
        when(wsEndpoint.setAllowedOriginPatterns(any())).thenReturn(wsEndpoint);
        
        when(sockjsEndpoint.addInterceptors(any())).thenReturn(sockjsEndpoint);
        when(sockjsEndpoint.setAllowedOriginPatterns(any())).thenReturn(sockjsEndpoint);
        when(sockjsEndpoint.withSockJS()).thenReturn(mock(org.springframework.web.socket.config.annotation.SockJsServiceRegistration.class));

        // When
        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);

        // Then
        verify(stompEndpointRegistry).addEndpoint("/ws");
        verify(stompEndpointRegistry).addEndpoint("/ws-sockjs");
    }

    @Test
    void registerStompEndpoints_ShouldAddJwtInterceptorToBothEndpoints() {
        // Given
        StompWebSocketEndpointRegistration wsEndpoint = mock(StompWebSocketEndpointRegistration.class);
        StompWebSocketEndpointRegistration sockjsEndpoint = mock(StompWebSocketEndpointRegistration.class);
        
        when(stompEndpointRegistry.addEndpoint("/ws")).thenReturn(wsEndpoint);
        when(stompEndpointRegistry.addEndpoint("/ws-sockjs")).thenReturn(sockjsEndpoint);
        
        when(wsEndpoint.addInterceptors(any())).thenReturn(wsEndpoint);
        when(wsEndpoint.setAllowedOriginPatterns(any())).thenReturn(wsEndpoint);
        
        when(sockjsEndpoint.addInterceptors(any())).thenReturn(sockjsEndpoint);
        when(sockjsEndpoint.setAllowedOriginPatterns(any())).thenReturn(sockjsEndpoint);
        when(sockjsEndpoint.withSockJS()).thenReturn(mock(org.springframework.web.socket.config.annotation.SockJsServiceRegistration.class));

        // When
        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);

        // Then
        verify(wsEndpoint).addInterceptors(jwtHandshakeInterceptor);
        verify(sockjsEndpoint).addInterceptors(jwtHandshakeInterceptor);
    }

    @Test
    void registerStompEndpoints_ShouldAllowAllOriginPatterns() {
        // Given
        StompWebSocketEndpointRegistration wsEndpoint = mock(StompWebSocketEndpointRegistration.class);
        StompWebSocketEndpointRegistration sockjsEndpoint = mock(StompWebSocketEndpointRegistration.class);
        
        when(stompEndpointRegistry.addEndpoint("/ws")).thenReturn(wsEndpoint);
        when(stompEndpointRegistry.addEndpoint("/ws-sockjs")).thenReturn(sockjsEndpoint);
        
        when(wsEndpoint.addInterceptors(any())).thenReturn(wsEndpoint);
        when(wsEndpoint.setAllowedOriginPatterns(any())).thenReturn(wsEndpoint);
        
        when(sockjsEndpoint.addInterceptors(any())).thenReturn(sockjsEndpoint);
        when(sockjsEndpoint.setAllowedOriginPatterns(any())).thenReturn(sockjsEndpoint);
        when(sockjsEndpoint.withSockJS()).thenReturn(mock(org.springframework.web.socket.config.annotation.SockJsServiceRegistration.class));

        // When
        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);

        // Then
        verify(wsEndpoint).setAllowedOriginPatterns("*");
        verify(sockjsEndpoint).setAllowedOriginPatterns("*");
    }

    @Test
    void registerStompEndpoints_ShouldEnableSockJSForSecondEndpoint() {
        // Given
        StompWebSocketEndpointRegistration wsEndpoint = mock(StompWebSocketEndpointRegistration.class);
        StompWebSocketEndpointRegistration sockjsEndpoint = mock(StompWebSocketEndpointRegistration.class);
        
        when(stompEndpointRegistry.addEndpoint("/ws")).thenReturn(wsEndpoint);
        when(stompEndpointRegistry.addEndpoint("/ws-sockjs")).thenReturn(sockjsEndpoint);
        
        when(wsEndpoint.addInterceptors(any())).thenReturn(wsEndpoint);
        when(wsEndpoint.setAllowedOriginPatterns(any())).thenReturn(wsEndpoint);
        
        when(sockjsEndpoint.addInterceptors(any())).thenReturn(sockjsEndpoint);
        when(sockjsEndpoint.setAllowedOriginPatterns(any())).thenReturn(sockjsEndpoint);
        when(sockjsEndpoint.withSockJS()).thenReturn(mock(org.springframework.web.socket.config.annotation.SockJsServiceRegistration.class));

        // When
        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);

        // Then
        verify(sockjsEndpoint).withSockJS();
        verify(wsEndpoint, never()).withSockJS();
    }
}
