package com.vn.caro_game.integrations.redis;

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
    void getFriendsOnlineStatus_WithNoFriends_ShouldReturnEmptyMap() {
        // Given
        Long userId = 1L;
        when(friendRepository.findAcceptedFriendshipsByUserId(userId)).thenReturn(new ArrayList<>());

        // When
        Map<Long, Boolean> result = redisService.getFriendsOnlineStatus(userId);

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
            createFriendship(userId, friendId1),
            createFriendship(userId, friendId2)
        );
        
        when(friendRepository.findAcceptedFriendshipsByUserId(userId)).thenReturn(friendships);
        when(redisTemplate.hasKey("online:2")).thenReturn(true);
        when(redisTemplate.hasKey("online:3")).thenReturn(true);

        // When
        Map<Long, Boolean> result = redisService.getFriendsOnlineStatus(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(friendId1));
        assertTrue(result.get(friendId2));
    }

    @Test
    void getFriendsOnlineStatus_WithFriendsAllOffline_ShouldReturnAllFalse() {
        // Given
        Long userId = 1L;
        Long friendId1 = 2L;
        Long friendId2 = 3L;
        
        List<Friend> friendships = Arrays.asList(
            createFriendship(userId, friendId1),
            createFriendship(userId, friendId2)
        );
        
        when(friendRepository.findAcceptedFriendshipsByUserId(userId)).thenReturn(friendships);
        when(redisTemplate.hasKey("online:2")).thenReturn(false);
        when(redisTemplate.hasKey("online:3")).thenReturn(false);

        // When
        Map<Long, Boolean> result = redisService.getFriendsOnlineStatus(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertFalse(result.get(friendId1));
        assertFalse(result.get(friendId2));
    }

    @Test
    void getFriendsOnlineStatus_WithMixedOnlineStatus_ShouldReturnCorrectStatus() {
        // Given
        Long userId = 1L;
        Long friendId1 = 2L;
        Long friendId2 = 3L;
        Long friendId3 = 4L;
        
        List<Friend> friendships = Arrays.asList(
            createFriendship(userId, friendId1),
            createFriendship(userId, friendId2),
            createFriendship(userId, friendId3)
        );
        
        when(friendRepository.findAcceptedFriendshipsByUserId(userId)).thenReturn(friendships);
        when(redisTemplate.hasKey("online:2")).thenReturn(true);
        when(redisTemplate.hasKey("online:3")).thenReturn(false);
        when(redisTemplate.hasKey("online:4")).thenReturn(true);

        // When
        Map<Long, Boolean> result = redisService.getFriendsOnlineStatus(userId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.get(friendId1));
        assertFalse(result.get(friendId2));
        assertTrue(result.get(friendId3));
    }

    @Test
    void getFriendsOnlineStatus_WithBidirectionalFriendships_ShouldHandleCorrectly() {
        // Given
        Long userId = 1L;
        Long friendId1 = 2L;
        
        Friend friendship = createReverseFriendship(friendId1, userId);
        List<Friend> friendships = Arrays.asList(friendship);
        
        when(friendRepository.findAcceptedFriendshipsByUserId(userId)).thenReturn(friendships);
        when(redisTemplate.hasKey("online:2")).thenReturn(true);

        // When
        Map<Long, Boolean> result = redisService.getFriendsOnlineStatus(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(friendId1));
        verify(redisTemplate).hasKey("online:2");
    }

    private Friend createFriendship(Long userId, Long friendId) {
        Friend friendship = new Friend();
        Friend.FriendId id = new Friend.FriendId(userId, friendId);
        friendship.setId(id);
        
        User user = new User();
        user.setId(userId);
        friendship.setUser(user);
        
        User friend = new User();
        friend.setId(friendId);
        friendship.setFriend(friend);
        
        return friendship;
    }

    private Friend createReverseFriendship(Long userId, Long friendId) {
        Friend friendship = new Friend();
        Friend.FriendId id = new Friend.FriendId(userId, friendId);
        friendship.setId(id);
        
        User user = new User();
        user.setId(userId);
        friendship.setUser(user);
        
        User friend = new User();
        friend.setId(friendId);
        friendship.setFriend(friend);
        
        return friendship;
    }
}
