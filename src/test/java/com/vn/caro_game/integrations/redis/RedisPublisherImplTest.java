package com.vn.caro_game.integrations.redis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit tests for RedisPublisherImpl.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class RedisPublisherImplTest {

    @Mock
    private RedisService redisService;

    @InjectMocks
    private RedisPublisherImpl redisPublisher;

    @Test
    void publishUserStatus_WithOnlineStatus_ShouldPublishCorrectMessage() {
        // Given
        Long userId = 1L;
        String status = "online";
        String expectedMessage = "{\"userId\":1,\"status\":\"online\"}";

        // When
        redisPublisher.publishUserStatus(userId, status);

        // Then
        verify(redisService).publish("user-status", expectedMessage);
    }

    @Test
    void publishUserStatus_WithOfflineStatus_ShouldPublishCorrectMessage() {
        // Given
        Long userId = 2L;
        String status = "offline";
        String expectedMessage = "{\"userId\":2,\"status\":\"offline\"}";

        // When
        redisPublisher.publishUserStatus(userId, status);

        // Then
        verify(redisService).publish("user-status", expectedMessage);
    }

    @Test
    void publishUserStatus_WithNullUserId_ShouldStillPublish() {
        // Given
        Long userId = null;
        String status = "online";
        String expectedMessage = "{\"userId\":null,\"status\":\"online\"}";

        // When
        redisPublisher.publishUserStatus(userId, status);

        // Then
        verify(redisService).publish("user-status", expectedMessage);
    }

    @Test
    void publishUserStatus_WithCustomStatus_ShouldPublishCorrectMessage() {
        // Given
        Long userId = 3L;
        String status = "in-game";
        String expectedMessage = "{\"userId\":3,\"status\":\"in-game\"}";

        // When
        redisPublisher.publishUserStatus(userId, status);

        // Then
        verify(redisService).publish("user-status", expectedMessage);
    }

    @Test
    void publishUserStatus_WithEmptyStatus_ShouldPublishCorrectMessage() {
        // Given
        Long userId = 4L;
        String status = "";
        String expectedMessage = "{\"userId\":4,\"status\":\"\"}";

        // When
        redisPublisher.publishUserStatus(userId, status);

        // Then
        verify(redisService).publish("user-status", expectedMessage);
    }
}
