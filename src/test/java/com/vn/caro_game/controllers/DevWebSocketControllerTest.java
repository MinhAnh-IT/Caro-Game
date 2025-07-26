package com.vn.caro_game.controllers;

import com.vn.caro_game.integrations.redis.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DevWebSocketController.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("dev")
class DevWebSocketControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private DevWebSocketController devWebSocketController;

    @Test
    void testEndpoint_ShouldReturnSuccessMessage() {
        // When
        String result = devWebSocketController.testEndpoint();

        // Then
        assertEquals("DevWebSocketController is working!", result);
    }

    @Test
    void testSendPong_ShouldSendMessageAndReturnConfirmation() {
        // When
        String result = devWebSocketController.testSendPong();

        // Then
        verify(messagingTemplate).convertAndSend("/topic/pong", "Test pong from server!");
        assertEquals("Pong message sent to /topic/pong", result);
    }

    @Test
    void handlePing_ShouldReturnPongMessage() {
        // Given
        String pingMessage = "Hello Server!";
        StompHeaderAccessor headerAccessor = mock(StompHeaderAccessor.class);

        // When
        String result = devWebSocketController.handlePing(pingMessage, headerAccessor);

        // Then
        assertTrue(result.startsWith("PONG: " + pingMessage + " (received at "));
        assertTrue(result.contains(String.valueOf(System.currentTimeMillis()).substring(0, 10))); // Check timestamp prefix
    }

    @Test
    void handlePing_WithEmptyMessage_ShouldReturnPongWithEmpty() {
        // Given
        String pingMessage = "";
        StompHeaderAccessor headerAccessor = mock(StompHeaderAccessor.class);

        // When
        String result = devWebSocketController.handlePing(pingMessage, headerAccessor);

        // Then
        assertTrue(result.startsWith("PONG:  (received at "));
    }

    @Test
    void handlePing_WithNullMessage_ShouldReturnPongWithNull() {
        // Given
        String pingMessage = null;
        StompHeaderAccessor headerAccessor = mock(StompHeaderAccessor.class);

        // When
        String result = devWebSocketController.handlePing(pingMessage, headerAccessor);

        // Then
        assertTrue(result.startsWith("PONG: null (received at "));
    }
}
