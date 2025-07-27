package com.vn.caro_game.integrations.redis;

public interface RedisPublisher {
    void publishUserStatus(Long userId, String status);
}
