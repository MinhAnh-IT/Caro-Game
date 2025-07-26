package com.vn.caro_game.integrations.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserStatusRedisSubscriber implements RedisSubscriber, MessageListener {
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(String channel, String message) {
        messagingTemplate.convertAndSend("/topic/user-status", message);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        onMessage(new String(pattern), new String(message.getBody()));
    }
}
