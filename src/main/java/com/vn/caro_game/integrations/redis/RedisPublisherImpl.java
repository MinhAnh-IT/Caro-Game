package com.vn.caro_game.integrations.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublisherImpl implements RedisPublisher {
    private final RedisService redisService;

    @Override
    public void publishUserStatus(Long userId, String status) {
        String msg = "{\"userId\":" + userId + ",\"status\":\"" + status + "\"}";
        redisService.publish("user-status", msg);
    }
}
