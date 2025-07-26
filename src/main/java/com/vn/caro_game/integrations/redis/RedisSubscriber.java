package com.vn.caro_game.integrations.redis;

/**
 * Redis subscriber interface for handling Pub/Sub messages.
 * 
 * <p>This interface defines the contract for Redis message subscribers
 * that can listen to Redis channels and process incoming messages.</p>
 * 
 * <h3>Usage Pattern:</h3>
 * <pre>{@code
 * RedisSubscriber subscriber = new MySubscriber();
 * redisService.subscribe("user-status", subscriber);
 * }</pre>
 * 
 * <h3>Message Processing:</h3>
 * <ul>
 *   <li>Receives channel name and message content</li>
 *   <li>Can be used for real-time notifications</li>
 *   <li>Supports multiple subscribers per channel</li>
 * </ul>
 * 
 * @author Caro Game Team
 * @since 1.0.0
 * @see RedisService
 * @see RedisServiceImpl
 */
public interface RedisSubscriber {
    
    /**
     * Handles incoming Redis Pub/Sub messages.
     * 
     * @param channel the channel name where the message was published
     * @param message the message content
     */
    void onMessage(String channel, String message);
}
