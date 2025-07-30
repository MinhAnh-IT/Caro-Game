package com.vn.caro_game.integrations.redis;

import com.vn.caro_game.dtos.response.FriendOnlineStatusResponse;
import com.vn.caro_game.entities.Friend;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.repositories.FriendRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RedisServiceImpl.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class RedisServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;
    
    @Mock
    private FriendRepository friendRepository;

    @InjectMocks
    private RedisServiceImpl redisService;

    @Test
    void setUserOnline_ShouldSetKeyWithTTL() {
        // Given
        Long userId = 1L;
        long ttlSeconds = 300L;
        String expectedKey = "online:" + userId;
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        redisService.setUserOnline(userId, ttlSeconds);

        // Then
        verify(valueOperations).set(expectedKey, "1", ttlSeconds, TimeUnit.SECONDS);
    }

    @Test
    void refreshUserOnline_ShouldSetValueAndTTL() {
        // Given
        Long userId = 1L;
        long ttlSeconds = 300L;
        String expectedKey = "online:" + userId;
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        redisService.refreshUserOnline(userId, ttlSeconds);

        // Then
        verify(valueOperations).set(expectedKey, "1", ttlSeconds, TimeUnit.SECONDS);
    }

    @Test
    void setUserOffline_ShouldDeleteKey() {
        // Given
        Long userId = 1L;
        String expectedKey = "online:" + userId;

        // When
        redisService.setUserOffline(userId);

        // Then
        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    void isUserOnline_WithExistingKey_ShouldReturnTrue() {
        // Given
        Long userId = 1L;
        String expectedKey = "online:" + userId;
        
        when(redisTemplate.hasKey(expectedKey)).thenReturn(true);

        // When
        boolean result = redisService.isUserOnline(userId);

        // Then
        assertTrue(result);
        verify(redisTemplate).hasKey(expectedKey);
    }

    @Test
    void isUserOnline_WithNonExistingKey_ShouldReturnFalse() {
        // Given
        Long userId = 1L;
        String expectedKey = "online:" + userId;
        
        when(redisTemplate.hasKey(expectedKey)).thenReturn(false);

        // When
        boolean result = redisService.isUserOnline(userId);

        // Then
        assertFalse(result);
        verify(redisTemplate).hasKey(expectedKey);
    }

    @Test
    void getUsersOnlineStatus_ShouldReturnStatusMap() {
        // Given
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        
        when(redisTemplate.hasKey("online:1")).thenReturn(true);
        when(redisTemplate.hasKey("online:2")).thenReturn(false);
        when(redisTemplate.hasKey("online:3")).thenReturn(true);

        // When
        Map<Long, Boolean> result = redisService.getUsersOnlineStatus(userIds);

        // Then
        assertEquals(3, result.size());
        assertTrue(result.get(1L));
        assertFalse(result.get(2L));
        assertTrue(result.get(3L));
        
        verify(redisTemplate).hasKey("online:1");
        verify(redisTemplate).hasKey("online:2");
        verify(redisTemplate).hasKey("online:3");
    }

    @Test
    void getUsersOnlineStatus_WithEmptyList_ShouldReturnEmptyMap() {
        // Given
        List<Long> userIds = Arrays.asList();

        // When
        Map<Long, Boolean> result = redisService.getUsersOnlineStatus(userIds);

        // Then
        assertTrue(result.isEmpty());
        verify(redisTemplate, never()).hasKey(anyString());
    }

    @Test
    void publish_ShouldCallRedisTemplate() {
        // Given
        String channel = "test-channel";
        String message = "test-message";

        // When
        redisService.publish(channel, message);

        // Then
        verify(redisTemplate).convertAndSend(channel, message);
    }

    @Test
    void subscribe_ShouldNotThrowException() {
        // Given
        String channel = "test-channel";
        RedisSubscriber subscriber = mock(RedisSubscriber.class);

        // When & Then
        assertDoesNotThrow(() -> redisService.subscribe(channel, subscriber));
    }

    @Test
    void getFriendsOnlineStatus_WithNoFriends_ShouldReturnEmptyList() {
        // Given
        Long userId = 1L;
        when(friendRepository.findAcceptedFriendshipsByUserId(userId)).thenReturn(new ArrayList<>());

        // When
        List<FriendOnlineStatusResponse> result = redisService.getFriendsOnlineStatus(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(friendRepository).findAcceptedFriendshipsByUserId(userId);
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void getFriendsOnlineStatus_WithFriendsAllOnline_ShouldReturnAllTrue() {
        // Given
        Long userId = 1L;
        Long friendId1 = 2L;
        Long friendId2 = 3L;
        
        List<Friend> friendships = Arrays.asList(
            createFriendship(userId, friendId1, "John Doe", "john.jpg"),
            createFriendship(userId, friendId2, "Jane Smith", "jane.jpg")
        );
        
        when(friendRepository.findAcceptedFriendshipsByUserId(userId)).thenReturn(friendships);
        when(redisTemplate.hasKey("online:2")).thenReturn(true);
        when(redisTemplate.hasKey("online:3")).thenReturn(true);

        // When
        List<FriendOnlineStatusResponse> result = redisService.getFriendsOnlineStatus(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        FriendOnlineStatusResponse friend1 = result.get(0);
        assertEquals(friendId1, friend1.getUserId());
        assertEquals("John Doe", friend1.getDisplayName());
        assertEquals("john.jpg", friend1.getAvatarUrl());
        assertTrue(friend1.getStatus());

        FriendOnlineStatusResponse friend2 = result.get(1);
        assertEquals(friendId2, friend2.getUserId());
        assertEquals("Jane Smith", friend2.getDisplayName());
        assertEquals("jane.jpg", friend2.getAvatarUrl());
        assertTrue(friend2.getStatus());
    }

    @Test
    void getFriendsOnlineStatus_WithFriendsAllOffline_ShouldReturnAllFalse() {
        // Given
        Long userId = 1L;
        Long friendId1 = 2L;
        Long friendId2 = 3L;
        
        List<Friend> friendships = Arrays.asList(
            createFriendship(userId, friendId1, "John Doe", "john.jpg"),
            createFriendship(userId, friendId2, "Jane Smith", "jane.jpg")
        );
        
        when(friendRepository.findAcceptedFriendshipsByUserId(userId)).thenReturn(friendships);
        when(redisTemplate.hasKey("online:2")).thenReturn(false);
        when(redisTemplate.hasKey("online:3")).thenReturn(false);

        // When
        List<FriendOnlineStatusResponse> result = redisService.getFriendsOnlineStatus(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        FriendOnlineStatusResponse friend1 = result.get(0);
        assertEquals(friendId1, friend1.getUserId());
        assertFalse(friend1.getStatus());

        FriendOnlineStatusResponse friend2 = result.get(1);
        assertEquals(friendId2, friend2.getUserId());
        assertFalse(friend2.getStatus());
    }

    @Test
    void getFriendsOnlineStatus_WithMixedOnlineStatus_ShouldReturnCorrectStatus() {
        // Given
        Long userId = 1L;
        Long friendId1 = 2L;
        Long friendId2 = 3L;
        Long friendId3 = 4L;
        
        List<Friend> friendships = Arrays.asList(
            createFriendship(userId, friendId1, "Alice Johnson", "alice.jpg"),
            createFriendship(userId, friendId2, "Bob Smith", "bob.jpg"),
            createFriendship(userId, friendId3, "Charlie Brown", "charlie.jpg")
        );
        
        when(friendRepository.findAcceptedFriendshipsByUserId(userId)).thenReturn(friendships);
        when(redisTemplate.hasKey("online:2")).thenReturn(true);
        when(redisTemplate.hasKey("online:3")).thenReturn(false);
        when(redisTemplate.hasKey("online:4")).thenReturn(true);

        // When
        List<FriendOnlineStatusResponse> result = redisService.getFriendsOnlineStatus(userId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        FriendOnlineStatusResponse friend1 = result.get(0);
        assertEquals(friendId1, friend1.getUserId());
        assertEquals("Alice Johnson", friend1.getDisplayName());
        assertTrue(friend1.getStatus());

        FriendOnlineStatusResponse friend2 = result.get(1);
        assertEquals(friendId2, friend2.getUserId());
        assertEquals("Bob Smith", friend2.getDisplayName());
        assertFalse(friend2.getStatus());

        FriendOnlineStatusResponse friend3 = result.get(2);
        assertEquals(friendId3, friend3.getUserId());
        assertEquals("Charlie Brown", friend3.getDisplayName());
        assertTrue(friend3.getStatus());
    }

    @Test
    void getFriendsOnlineStatus_WithBidirectionalFriendships_ShouldHandleCorrectly() {
        // Given
        Long userId = 1L;
        Long friendId1 = 2L;
        
        Friend friendship = createReverseFriendship(friendId1, userId, "Diana Prince", "diana.jpg");
        List<Friend> friendships = Arrays.asList(friendship);
        
        when(friendRepository.findAcceptedFriendshipsByUserId(userId)).thenReturn(friendships);
        when(redisTemplate.hasKey("online:2")).thenReturn(true);

        // When
        List<FriendOnlineStatusResponse> result = redisService.getFriendsOnlineStatus(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        FriendOnlineStatusResponse friend = result.get(0);
        assertEquals(friendId1, friend.getUserId());
        assertEquals("Diana Prince", friend.getDisplayName());
        assertEquals("diana.jpg", friend.getAvatarUrl());
        assertTrue(friend.getStatus());

        verify(redisTemplate).hasKey("online:2");
    }

    private Friend createFriendship(Long userId, Long friendId, String displayName, String avatarUrl) {
        Friend friendship = new Friend();
        Friend.FriendId id = new Friend.FriendId(userId, friendId);
        friendship.setId(id);
        
        User user = new User();
        user.setId(userId);
        friendship.setUser(user);
        
        User friend = new User();
        friend.setId(friendId);
        friend.setDisplayName(displayName);
        friend.setAvatarUrl(avatarUrl);
        friendship.setFriend(friend);
        
        return friendship;
    }

    private Friend createReverseFriendship(Long userId, Long friendId, String displayName, String avatarUrl) {
        Friend friendship = new Friend();
        Friend.FriendId id = new Friend.FriendId(userId, friendId);
        friendship.setId(id);
        
        User user = new User();
        user.setId(userId);
        user.setDisplayName(displayName);
        user.setAvatarUrl(avatarUrl);
        friendship.setUser(user);
        
        User friend = new User();
        friend.setId(friendId);
        friendship.setFriend(friend);
        
        return friendship;
    }
}
