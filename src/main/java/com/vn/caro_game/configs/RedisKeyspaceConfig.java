package com.vn.caro_game.configs;

import com.vn.caro_game.integrations.redis.UserStatusRedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisKeyspaceConfig {
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            UserStatusRedisSubscriber userStatusRedisSubscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // Lắng nghe keyspace event cho key online:*
        container.addMessageListener(userStatusRedisSubscriber, new PatternTopic("__keyevent@*__:expired"));
        // Lắng nghe pub/sub user-status
        container.addMessageListener(userStatusRedisSubscriber, new ChannelTopic("user-status"));
        return container;
    }
}
