package com.vn.caro_game.controllers;

import com.vn.caro_game.configs.CustomUserDetails;
import com.vn.caro_game.dtos.response.ApiResponse;
import com.vn.caro_game.dtos.response.FriendOnlineStatusResponse;
import com.vn.caro_game.integrations.redis.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

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
    private CustomUserDetails customUserDetails;

    @InjectMocks
    private OnlineStatusController onlineStatusController;

    @Test
    void getFriendsOnlineStatus_WithValidUser_ShouldReturnFriendsStatusList() {
        // Given
        Long userId = 1L;

        List<FriendOnlineStatusResponse> expectedFriendsList = new ArrayList<>();
        expectedFriendsList.add(new FriendOnlineStatusResponse(2L, "John Doe", "avatar1.jpg", true));
        expectedFriendsList.add(new FriendOnlineStatusResponse(3L, "Jane Smith", "avatar2.jpg", false));
        expectedFriendsList.add(new FriendOnlineStatusResponse(4L, "Bob Wilson", "avatar3.jpg", true));

        when(customUserDetails.getUserId()).thenReturn(userId);
        when(redisService.getFriendsOnlineStatus(userId)).thenReturn(expectedFriendsList);

        // When
        ResponseEntity<ApiResponse<List<FriendOnlineStatusResponse>>> response =
            onlineStatusController.getFriendsOnlineStatus(customUserDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Friends online status retrieved successfully", response.getBody().getMessage());
        assertEquals(expectedFriendsList, response.getBody().getData());

        verify(customUserDetails).getUserId();
        verify(redisService).getFriendsOnlineStatus(userId);
    }

    @Test
    void getFriendsOnlineStatus_WithEmptyFriendsList_ShouldReturnEmptyList() {
        // Given
        Long userId = 1L;

        List<FriendOnlineStatusResponse> emptyFriendsList = new ArrayList<>();

        when(customUserDetails.getUserId()).thenReturn(userId);
        when(redisService.getFriendsOnlineStatus(userId)).thenReturn(emptyFriendsList);

        // When
        ResponseEntity<ApiResponse<List<FriendOnlineStatusResponse>>> response =
            onlineStatusController.getFriendsOnlineStatus(customUserDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Friends online status retrieved successfully", response.getBody().getMessage());
        assertTrue(response.getBody().getData().isEmpty());
        
        verify(customUserDetails).getUserId();
        verify(redisService).getFriendsOnlineStatus(userId);
    }

    @Test
    void getFriendsOnlineStatus_WithMixedOnlineStatus_ShouldReturnCorrectStatuses() {
        // Given
        Long userId = 1L;

        List<FriendOnlineStatusResponse> mixedFriendsList = new ArrayList<>();
        mixedFriendsList.add(new FriendOnlineStatusResponse(2L, "Alice Johnson", "alice.jpg", true));
        mixedFriendsList.add(new FriendOnlineStatusResponse(3L, "Bob Smith", "bob.jpg", false));
        mixedFriendsList.add(new FriendOnlineStatusResponse(4L, "Charlie Brown", "charlie.jpg", true));
        mixedFriendsList.add(new FriendOnlineStatusResponse(5L, "Diana Prince", "diana.jpg", false));

        when(customUserDetails.getUserId()).thenReturn(userId);
        when(redisService.getFriendsOnlineStatus(userId)).thenReturn(mixedFriendsList);

        // When
        ResponseEntity<ApiResponse<List<FriendOnlineStatusResponse>>> response =
            onlineStatusController.getFriendsOnlineStatus(customUserDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Friends online status retrieved successfully", response.getBody().getMessage());
        
        List<FriendOnlineStatusResponse> responseData = response.getBody().getData();
        assertEquals(4, responseData.size());

        // Verify specific friend statuses
        assertEquals(2L, responseData.get(0).getUserId());
        assertEquals("Alice Johnson", responseData.get(0).getDisplayName());
        assertTrue(responseData.get(0).getStatus());

        assertEquals(3L, responseData.get(1).getUserId());
        assertEquals("Bob Smith", responseData.get(1).getDisplayName());
        assertFalse(responseData.get(1).getStatus());

        verify(customUserDetails).getUserId();
        verify(redisService).getFriendsOnlineStatus(userId);
    }

    @Test
    void getFriendsOnlineStatus_WithLargeNumberOfFriends_ShouldHandleCorrectly() {
        // Given
        Long userId = 1L;

        List<FriendOnlineStatusResponse> largeFriendsList = new ArrayList<>();
        for (int i = 2; i <= 20; i++) {
            boolean isOnline = i % 2 == 0; // Every even friend is online
            largeFriendsList.add(new FriendOnlineStatusResponse(
                (long) i,
                "Friend " + i,
                "avatar" + i + ".jpg",
                isOnline
            ));
        }

        when(customUserDetails.getUserId()).thenReturn(userId);
        when(redisService.getFriendsOnlineStatus(userId)).thenReturn(largeFriendsList);

        // When
        ResponseEntity<ApiResponse<List<FriendOnlineStatusResponse>>> response =
            onlineStatusController.getFriendsOnlineStatus(customUserDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Friends online status retrieved successfully", response.getBody().getMessage());
        assertEquals(19, response.getBody().getData().size()); // 19 friends (2-20)

        verify(customUserDetails).getUserId();
        verify(redisService).getFriendsOnlineStatus(userId);
    }
}