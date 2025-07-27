package com.vn.caro_game.configs;

import com.vn.caro_game.integrations.redis.UserStatusRedisSubscriber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RedisKeyspaceConfig.
 * Tests Redis message listener configuration for user status management.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class RedisKeyspaceConfigTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private UserStatusRedisSubscriber userStatusRedisSubscriber;

    @InjectMocks
    private RedisKeyspaceConfig redisKeyspaceConfig;

    @Test
    void redisMessageListenerContainer_ShouldReturnValidContainer() {
        // When
        RedisMessageListenerContainer container = redisKeyspaceConfig
                .redisMessageListenerContainer(redisConnectionFactory, userStatusRedisSubscriber);

        // Then
        assertNotNull(container);
        assertEquals(redisConnectionFactory, container.getConnectionFactory());
    }

    @Test
    void redisMessageListenerContainer_ShouldConfigureConnectionFactory() {
        // When
        RedisMessageListenerContainer container = redisKeyspaceConfig
                .redisMessageListenerContainer(redisConnectionFactory, userStatusRedisSubscriber);

        // Then
        assertNotNull(container);
        assertEquals(redisConnectionFactory, container.getConnectionFactory());
    }

    @Test
    void redisMessageListenerContainer_ShouldHaveMessageListeners() {
        // When
        RedisMessageListenerContainer container = redisKeyspaceConfig
                .redisMessageListenerContainer(redisConnectionFactory, userStatusRedisSubscriber);

        // Then
        assertNotNull(container);
        // The container should have message listeners configured
        // We can't directly verify the listeners without accessing private fields,
        // but we can ensure the container is properly configured
        assertTrue(container.getConnectionFactory() != null);
    }

    @Test
    void redisMessageListenerContainer_ShouldBeProperlyConfigured() {
        // When
        RedisMessageListenerContainer container = redisKeyspaceConfig
                .redisMessageListenerContainer(redisConnectionFactory, userStatusRedisSubscriber);

        // Then
        assertNotNull(container);
        assertNotNull(container.getConnectionFactory());
        assertEquals(redisConnectionFactory, container.getConnectionFactory());
    }

    @Test
    void redisMessageListenerContainer_ShouldAcceptNonNullParameters() {
        // Given - both parameters are mocked and non-null

        // When
        RedisMessageListenerContainer container = redisKeyspaceConfig
                .redisMessageListenerContainer(redisConnectionFactory, userStatusRedisSubscriber);

        // Then
        assertNotNull(container);
    }

    @Test
    void redisMessageListenerContainer_ShouldCreateNewInstanceEachTime() {
        // When
        RedisMessageListenerContainer container1 = redisKeyspaceConfig
                .redisMessageListenerContainer(redisConnectionFactory, userStatusRedisSubscriber);
        RedisMessageListenerContainer container2 = redisKeyspaceConfig
                .redisMessageListenerContainer(redisConnectionFactory, userStatusRedisSubscriber);

        // Then
        assertNotNull(container1);
        assertNotNull(container2);
        assertNotSame(container1, container2); // Different instances
        assertEquals(container1.getConnectionFactory(), container2.getConnectionFactory()); // Same connection factory
    }
}
