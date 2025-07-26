package com.vn.caro_game.controllers;

import com.vn.caro_game.dtos.response.ApiResponse;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.integrations.redis.RedisService;
import com.vn.caro_game.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;

    @InjectMocks
    private OnlineStatusController onlineStatusController;
    
    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getFriendsOnlineStatus_WithValidUser_ShouldReturnFriendsStatusMap() {
        // Given
        String userEmail = "test@example.com";
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail(userEmail);
        
        Map<Long, Boolean> expectedFriendsStatus = new HashMap<>();
        expectedFriendsStatus.put(2L, true);   // Friend 2 is online
        expectedFriendsStatus.put(3L, false);  // Friend 3 is offline
        expectedFriendsStatus.put(4L, true);   // Friend 4 is online
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(redisService.getFriendsOnlineStatus(userId)).thenReturn(expectedFriendsStatus);

        // When
        ResponseEntity<ApiResponse<Map<Long, Boolean>>> response = onlineStatusController.getFriendsOnlineStatus();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Friends online status retrieved successfully", response.getBody().getMessage());
        assertEquals(expectedFriendsStatus, response.getBody().getData());
        
        verify(userRepository).findByEmail(userEmail);
        verify(redisService).getFriendsOnlineStatus(userId);
    }

    @Test
    void getFriendsOnlineStatus_WithEmptyFriendsList_ShouldReturnEmptyMap() {
        // Given
        String userEmail = "test@example.com";
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail(userEmail);
        
        Map<Long, Boolean> emptyFriendsStatus = new HashMap<>();
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(redisService.getFriendsOnlineStatus(userId)).thenReturn(emptyFriendsStatus);

        // When
        ResponseEntity<ApiResponse<Map<Long, Boolean>>> response = onlineStatusController.getFriendsOnlineStatus();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Friends online status retrieved successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertTrue(response.getBody().getData().isEmpty());
        
        verify(userRepository).findByEmail(userEmail);
        verify(redisService).getFriendsOnlineStatus(userId);
    }

    @Test
    void getFriendsOnlineStatus_WithUserNotFound_ShouldThrowCustomException() {
        // Given
        String userEmail = "nonexistent@example.com";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomException.class, () -> {
            onlineStatusController.getFriendsOnlineStatus();
        });
        
        verify(userRepository).findByEmail(userEmail);
        verify(redisService, never()).getFriendsOnlineStatus(anyLong());
    }

    @Test
    void getFriendsOnlineStatus_WithMixedOnlineStatus_ShouldReturnCorrectStatuses() {
        // Given
        String userEmail = "test@example.com";
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail(userEmail);
        
        Map<Long, Boolean> mixedFriendsStatus = new HashMap<>();
        mixedFriendsStatus.put(2L, true);    // Friend 2 is online
        mixedFriendsStatus.put(3L, false);   // Friend 3 is offline
        mixedFriendsStatus.put(4L, true);    // Friend 4 is online
        mixedFriendsStatus.put(5L, false);   // Friend 5 is offline
        mixedFriendsStatus.put(6L, false);   // Friend 6 is offline
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(redisService.getFriendsOnlineStatus(userId)).thenReturn(mixedFriendsStatus);

        // When
        ResponseEntity<ApiResponse<Map<Long, Boolean>>> response = onlineStatusController.getFriendsOnlineStatus();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Friends online status retrieved successfully", response.getBody().getMessage());
        
        Map<Long, Boolean> responseData = response.getBody().getData();
        assertNotNull(responseData);
        assertEquals(5, responseData.size());
        
        // Check specific statuses
        assertTrue(responseData.get(2L));   // Friend 2 is online
        assertFalse(responseData.get(3L));  // Friend 3 is offline
        assertTrue(responseData.get(4L));   // Friend 4 is online
        assertFalse(responseData.get(5L));  // Friend 5 is offline
        assertFalse(responseData.get(6L));  // Friend 6 is offline
        
        verify(userRepository).findByEmail(userEmail);
        verify(redisService).getFriendsOnlineStatus(userId);
    }
}