package com.vn.caro_game.integrations.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.lang.Nullable;

@Component
@RequiredArgsConstructor
public class UserStatusRedisSubscriber implements RedisSubscriber, MessageListener {
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(String channel, String message) {
        messagingTemplate.convertAndSend("/topic/user-status", message);
    }

    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {
        String patternStr = pattern != null ? new String(pattern) : null;
        String messageStr = new String(message.getBody());
        onMessage(patternStr, messageStr);
    }
}
