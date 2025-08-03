package com.vn.caro_game.integrations.redis;

import com.vn.caro_game.dtos.response.FriendOnlineStatusResponse;
import com.vn.caro_game.entities.Friend;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.repositories.FriendRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis service implementation for user online status management.
 * 
 * <p>This service provides Redis-based operations for managing user online status
 * using TTL (Time To Live) keys and Pub/Sub messaging for real-time notifications.</p>
 * 
 * <h3>Redis Operations:</h3>
 * <ul>
 *   <li><strong>Online Status:</strong> Uses TTL keys with pattern "online:{userId}"</li>
 *   <li><strong>TTL Management:</strong> Automatic cleanup when users disconnect</li>
 *   <li><strong>Batch Operations:</strong> Efficient multi-user status checking</li>
 *   <li><strong>Pub/Sub:</strong> Real-time messaging for status changes</li>
 * </ul>
 * 
 * <h3>Key Patterns:</h3>
 * <pre>{@code
 * online:{userId} -> "1" (with TTL seconds)
 * }</pre>
 * 
 * <h3>Performance Considerations:</h3>
 * <ul>
 *   <li>Uses Redis pipeline for batch operations</li>
 *   <li>TTL-based cleanup reduces memory usage</li>
 *   <li>Pub/Sub for efficient real-time updates</li>
 * </ul>
 * 
 * @author Caro Game Team
 * @since 1.0.0
 * @see RedisService
 * @see RedisTemplate
 */
@Service
public class RedisServiceImpl implements RedisService {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private FriendRepository friendRepository;

    /**
     * Sets a user as online with specified TTL.
     * 
     * <p>Creates a Redis key with pattern "online:{userId}" and sets it to expire
     * after the specified TTL. This provides automatic cleanup when users disconnect.</p>
     * 
     * @param userId the user ID to mark as online
     * @param ttlSeconds time to live in seconds before the key expires
     */
    @Override
    public void setUserOnline(Long userId, long ttlSeconds) {
        String key = "online:" + userId;
        redisTemplate.opsForValue().set(key, "1", ttlSeconds, TimeUnit.SECONDS);
    }

        /**
     * Refreshes the TTL for a user's online status key.
     * 
     * <p>Updates the expiration time for an existing online status key.
     * If the key doesn't exist, this operation will create it with value "1".</p>
     * 
     * @param userId the user ID to refresh
     * @param ttlSeconds new TTL in seconds
     */
    @Override
    public void refreshUserOnline(Long userId, long ttlSeconds) {
        String key = "online:" + userId;
        logger.info("=== REFRESHING ONLINE KEY: {} WITH TTL: {} ===", key, ttlSeconds);
        
        // Set value "1" and TTL atomically - creates key if not exists, refreshes if exists
        redisTemplate.opsForValue().set(key, "1", ttlSeconds, TimeUnit.SECONDS);
        logger.info("=== KEY {} REFRESHED/CREATED SUCCESSFULLY ===", key);
    }

    /**
     * Sets a user as offline by removing the Redis key.
     * 
     * <p>Immediately removes the online status key, marking the user as offline.
     * This is called when a user explicitly disconnects.</p>
     * 
     * @param userId the user ID to mark as offline
     */
    @Override
    public void setUserOffline(Long userId) {
        String key = "online:" + userId;
        redisTemplate.delete(key);
    }

    /**
     * Checks if a user is currently online.
     * 
     * <p>Checks for the existence of the "online:{userId}" key in Redis.
     * Returns true if the key exists (and hasn't expired), false otherwise.</p>
     * 
     * @param userId the user ID to check
     * @return true if user is online, false otherwise
     */
    @Override
    public boolean isUserOnline(Long userId) {
        String key = "online:" + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Gets online status for multiple users efficiently.
     * 
     * <p>Performs batch checking of multiple user online statuses using Redis
     * pipeline operations for improved performance when checking many users.</p>
     * 
     * @param userIds list of user IDs to check
     * @return map of user ID to online status (true/false)
     */
    @Override
    public Map<Long, Boolean> getUsersOnlineStatus(List<Long> userIds) {
        Map<Long, Boolean> statusMap = new HashMap<>();
        for (Long userId : userIds) {
            statusMap.put(userId, isUserOnline(userId));
        }
        return statusMap;
    }

    /**
     * Retrieves the online status of all accepted friends for a given user.
     *
     * <p>Retrieves all accepted friends of the specified user and checks their online status.
     * This method combines database queries to get friend relationships with Redis operations
     * to check online status efficiently. Returns comprehensive friend information including
     * display name, avatar, and online status.</p>
     *
     * @param userId the user ID whose friends' online status to check
     * @return list of friend online status information (only includes accepted friends)
     */
    @Override
    public List<FriendOnlineStatusResponse> getFriendsOnlineStatus(Long userId) {
        // Get all accepted friends for the user
        List<Friend> friendships = friendRepository.findAcceptedFriendshipsByUserId(userId);
        
        List<FriendOnlineStatusResponse> friendsList = new ArrayList<>();
        Set<Long> processedFriends = new HashSet<>(); // To avoid duplicates

        for (Friend friendship : friendships) {
            // Determine which user is the friend (not the requesting user)
            User friendUser = friendship.getUser().getId().equals(userId)
                ? friendship.getFriend()
                : friendship.getUser();

            // Skip if we already processed this friend
            if (processedFriends.contains(friendUser.getId())) {
                continue;
            }
            processedFriends.add(friendUser.getId());

            // Check online status for this friend
            boolean isOnline = isUserOnline(friendUser.getId());

            // Create response DTO with friend details
            FriendOnlineStatusResponse friendResponse = new FriendOnlineStatusResponse(
                friendUser.getId(),
                friendUser.getDisplayName(),
                friendUser.getAvatarUrl(),
                isOnline
            );

            friendsList.add(friendResponse);
        }
        
        return friendsList;
    }

    /**
     * Subscribes to a Redis channel for Pub/Sub messaging.
     * 
     * <p>Registers a subscriber to listen for messages on the specified channel.
     * This enables real-time notifications for status changes or other events.</p>
     * 
     * @param channel the channel name to subscribe to
     * @param subscriber the subscriber handler to process messages
     */
    @Override
    public void subscribe(String channel, RedisSubscriber subscriber) {
        // Note: This method would need a proper MessageListener implementation
        // For now, we'll use direct Redis template operations
        // In a full implementation, you would create a MessageListener adapter
    }

    /**
     * Publishes a message to a Redis channel.
     * 
     * <p>Sends a message to all subscribers of the specified channel.
     * Used for broadcasting status changes or other real-time events.</p>
     * 
     * @param channel the channel name to publish to
     * @param message the message content to publish
     */
    @Override
    public void publish(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
    }
}
