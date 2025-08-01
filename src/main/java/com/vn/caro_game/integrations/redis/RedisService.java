package com.vn.caro_game.integrations.redis;

import com.vn.caro_game.dtos.response.FriendOnlineStatusResponse;

import java.util.List;
import java.util.Map;

/**
 * Redis service interface for user online status management.
 * 
 * <p>This service provides Redis-based operations for managing user online status
 * with TTL (Time To Live) support and Pub/Sub messaging capabilities.</p>
 * 
 * <h3>Online Status Management:</h3>
 * <ul>
 *   <li>TTL-based online status tracking</li>
 *   <li>Batch status checking for multiple users</li>
 *   <li>Automatic cleanup through Redis key expiration</li>
 * </ul>
 * 
 * <h3>Redis Key Pattern:</h3>
 * <pre>{@code
 * online:{userId} -> "1" (with TTL)
 * }</pre>
 * 
 * @author Caro Game Team
 * @since 1.0.0
 * @see RedisServiceImpl
 * @see RedisPublisher
 * @see RedisSubscriber
 */
public interface RedisService {
    
    /**
     * Sets a user as online with specified TTL.
     * 
     * @param userId the user ID
     * @param ttlSeconds time to live in seconds
     */
    void setUserOnline(Long userId, long ttlSeconds);
    
    /**
     * Refreshes user online status TTL.
     * 
     * @param userId the user ID
     * @param ttlSeconds new TTL in seconds
     */
    void refreshUserOnline(Long userId, long ttlSeconds);
    
    /**
     * Sets a user as offline by removing the Redis key.
     * 
     * @param userId the user ID
     */
    void setUserOffline(Long userId);
    
    /**
     * Checks if a user is currently online.
     * 
     * @param userId the user ID
     * @return true if user is online, false otherwise
     */
    boolean isUserOnline(Long userId);
    
    /**
     * Gets online status for multiple users.
     * 
     * @param userIds list of user IDs to check
     * @return map of user ID to online status
     */
    Map<Long, Boolean> getUsersOnlineStatus(List<Long> userIds);
    
    /**
     * Retrieves the online status of all accepted friends for a given user.
     *
     * <p>This method fetches all accepted friendships for the specified user and
     * checks their online status using Redis. It returns comprehensive friend
     * information including display name, avatar, and online status.</p>
     *
     * @param userId the user ID whose friends' online status to check
     * @return list of friend online status information (only includes accepted friends)
     */
    List<FriendOnlineStatusResponse> getFriendsOnlineStatus(Long userId);

    /**
     * Subscribes to a Redis channel.
     * 
     * @param channel the channel name
     * @param subscriber the subscriber handler
     */
    void subscribe(String channel, RedisSubscriber subscriber);
    
    /**
     * Publishes a message to a Redis channel.
     * 
     * @param channel the channel name
     * @param message the message to publish
     */
    void publish(String channel, String message);
}
