package com.vn.caro_game.controllers;

import com.vn.caro_game.integrations.redis.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OnlineStatusController.
 * Tests the friends online status retrieval functionality.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class OnlineStatusControllerTest {

    @Mock
    private RedisService redisService;

    @InjectMocks
    private OnlineStatusController onlineStatusController;

    @Test
    void getFriendsOnlineStatus_WithValidUserId_ShouldReturnFriendsStatusMap() {
        // Given
        Long userId = 1L;
        Map<Long, Boolean> expectedFriendsStatus = new HashMap<>();
        expectedFriendsStatus.put(2L, true);   // Friend 2 is online
        expectedFriendsStatus.put(3L, false);  // Friend 3 is offline
        expectedFriendsStatus.put(4L, true);   // Friend 4 is online
        
        when(redisService.getFriendsOnlineStatus(userId)).thenReturn(expectedFriendsStatus);

        // When
        ResponseEntity<Map<Long, Boolean>> response = onlineStatusController.getFriendsOnlineStatus(userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedFriendsStatus, response.getBody());
        
        Map<Long, Boolean> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(3, responseBody.size());
        assertTrue(responseBody.get(2L));   // Friend 2 is online
        assertFalse(responseBody.get(3L));  // Friend 3 is offline
        assertTrue(responseBody.get(4L));   // Friend 4 is online
        verify(redisService).getFriendsOnlineStatus(userId);
    }

    @Test
    void getFriendsOnlineStatus_WithUserHavingNoFriends_ShouldReturnEmptyMap() {
        // Given
        Long userId = 1L;
        Map<Long, Boolean> emptyFriendsStatus = new HashMap<>();
        
        when(redisService.getFriendsOnlineStatus(userId)).thenReturn(emptyFriendsStatus);

        // When
        ResponseEntity<Map<Long, Boolean>> response = onlineStatusController.getFriendsOnlineStatus(userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<Long, Boolean> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.isEmpty());
        verify(redisService).getFriendsOnlineStatus(userId);
    }

    @Test
    void getFriendsOnlineStatus_WithAllFriendsOnline_ShouldReturnAllTrue() {
        // Given
        Long userId = 1L;
        Map<Long, Boolean> expectedFriendsStatus = new HashMap<>();
        expectedFriendsStatus.put(2L, true);
        expectedFriendsStatus.put(3L, true);
        expectedFriendsStatus.put(4L, true);
        
        when(redisService.getFriendsOnlineStatus(userId)).thenReturn(expectedFriendsStatus);

        // When
        ResponseEntity<Map<Long, Boolean>> response = onlineStatusController.getFriendsOnlineStatus(userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<Long, Boolean> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(3, responseBody.size());
        responseBody.values().forEach(status -> assertTrue(status));
        verify(redisService).getFriendsOnlineStatus(userId);
    }

    @Test
    void getFriendsOnlineStatus_WithAllFriendsOffline_ShouldReturnAllFalse() {
        // Given
        Long userId = 1L;
        Map<Long, Boolean> expectedFriendsStatus = new HashMap<>();
        expectedFriendsStatus.put(2L, false);
        expectedFriendsStatus.put(3L, false);
        expectedFriendsStatus.put(4L, false);
        
        when(redisService.getFriendsOnlineStatus(userId)).thenReturn(expectedFriendsStatus);

        // When
        ResponseEntity<Map<Long, Boolean>> response = onlineStatusController.getFriendsOnlineStatus(userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<Long, Boolean> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(3, responseBody.size());
        responseBody.values().forEach(status -> assertFalse(status));
        verify(redisService).getFriendsOnlineStatus(userId);
    }

    @Test
    void getFriendsOnlineStatus_WithSingleFriend_ShouldReturnSingleStatus() {
        // Given
        Long userId = 1L;
        Map<Long, Boolean> expectedFriendsStatus = new HashMap<>();
        expectedFriendsStatus.put(2L, true);
        
        when(redisService.getFriendsOnlineStatus(userId)).thenReturn(expectedFriendsStatus);

        // When
        ResponseEntity<Map<Long, Boolean>> response = onlineStatusController.getFriendsOnlineStatus(userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<Long, Boolean> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.get(2L));
        verify(redisService).getFriendsOnlineStatus(userId);
    }
}
