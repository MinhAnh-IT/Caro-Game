package com.vn.caro_game.configs;

import com.vn.caro_game.integrations.jwt.JwtService;
import com.vn.caro_game.integrations.redis.RedisService;
import com.vn.caro_game.integrations.redis.RedisPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebSocketEventListener.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class WebSocketEventListenerTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisPublisher redisPublisher;

    @InjectMocks
    private WebSocketEventListener webSocketEventListener;

    @Mock
    private SessionConnectEvent connectEvent;

    @Mock
    private SessionDisconnectEvent disconnectEvent;

    @Test
    void webSocketEventListener_ShouldBeCreated() {
        // Test that the listener can be instantiated
        assertNotNull(webSocketEventListener);
        assertNotNull(jwtService);
        assertNotNull(redisService);
        assertNotNull(redisPublisher);
    }

    @Test
    void handleWebSocketConnectListener_WithNullEvent_ShouldNotThrowException() {
        // When & Then
        assertDoesNotThrow(() -> webSocketEventListener.handleWebSocketConnectListener(null));
    }

    @Test
    void handleWebSocketDisconnectListener_WithNullEvent_ShouldNotThrowException() {
        // When & Then
        assertDoesNotThrow(() -> webSocketEventListener.handleWebSocketDisconnectListener(null));
    }

    @Test
    void handleWebSocketConnectListener_WithMockEvent_ShouldNotThrowException() {
        // When & Then
        assertDoesNotThrow(() -> webSocketEventListener.handleWebSocketConnectListener(connectEvent));
    }

    @Test
    void handleWebSocketDisconnectListener_WithMockEvent_ShouldNotThrowException() {
        // When & Then
        assertDoesNotThrow(() -> webSocketEventListener.handleWebSocketDisconnectListener(disconnectEvent));
    }
}
