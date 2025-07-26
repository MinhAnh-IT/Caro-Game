package com.vn.caro_game.configs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for RedisConfig.
 * Tests Redis connection and template configuration.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class RedisConfigTest {

    @InjectMocks
    private RedisConfig redisConfig;

    @Test
    void jedisConnectionFactory_ShouldReturnValidFactory() {
        // Given
        ReflectionTestUtils.setField(redisConfig, "redisHost", "localhost");
        ReflectionTestUtils.setField(redisConfig, "redisPort", 6379);
        ReflectionTestUtils.setField(redisConfig, "redisPassword", "");

        // When
        JedisConnectionFactory connectionFactory = redisConfig.jedisConnectionFactory();

        // Then
        assertNotNull(connectionFactory);
        assertTrue(connectionFactory instanceof JedisConnectionFactory);
    }

    @Test
    void jedisConnectionFactory_ShouldConfigureHostAndPort() {
        // Given
        String testHost = "test-redis-host";
        int testPort = 6380;
        ReflectionTestUtils.setField(redisConfig, "redisHost", testHost);
        ReflectionTestUtils.setField(redisConfig, "redisPort", testPort);
        ReflectionTestUtils.setField(redisConfig, "redisPassword", "");

        // When
        JedisConnectionFactory connectionFactory = redisConfig.jedisConnectionFactory();

        // Then
        assertNotNull(connectionFactory);
        var standaloneConfig = connectionFactory.getStandaloneConfiguration();
        assertNotNull(standaloneConfig);
        assertEquals(testHost, standaloneConfig.getHostName());
        assertEquals(testPort, standaloneConfig.getPort());
    }

    @Test
    void jedisConnectionFactory_ShouldConfigurePasswordWhenProvided() {
        // Given
        String testPassword = "test-password";
        ReflectionTestUtils.setField(redisConfig, "redisHost", "localhost");
        ReflectionTestUtils.setField(redisConfig, "redisPort", 6379);
        ReflectionTestUtils.setField(redisConfig, "redisPassword", testPassword);

        // When
        JedisConnectionFactory connectionFactory = redisConfig.jedisConnectionFactory();

        // Then
        assertNotNull(connectionFactory);
        var standaloneConfig = connectionFactory.getStandaloneConfiguration();
        assertNotNull(standaloneConfig);
        var password = standaloneConfig.getPassword();
        assertTrue(password.isPresent());
        // RedisPassword masks the actual password in toString(), so we check if it's present and not empty
        assertNotNull(password.get());
        assertFalse(password.get().toString().isEmpty());
    }

    @Test
    void jedisConnectionFactory_ShouldNotConfigurePasswordWhenEmpty() {
        // Given
        ReflectionTestUtils.setField(redisConfig, "redisHost", "localhost");
        ReflectionTestUtils.setField(redisConfig, "redisPort", 6379);
        ReflectionTestUtils.setField(redisConfig, "redisPassword", "");

        // When
        JedisConnectionFactory connectionFactory = redisConfig.jedisConnectionFactory();

        // Then
        assertNotNull(connectionFactory);
        var standaloneConfig = connectionFactory.getStandaloneConfiguration();
        assertNotNull(standaloneConfig);
        assertFalse(standaloneConfig.getPassword().isPresent());
    }

    @Test
    void redisTemplate_ShouldReturnValidTemplate() {
        // Given
        RedisConnectionFactory mockConnectionFactory = mock(RedisConnectionFactory.class);

        // When
        RedisTemplate<String, Object> redisTemplate = redisConfig.redisTemplate(mockConnectionFactory);

        // Then
        assertNotNull(redisTemplate);
        assertEquals(mockConnectionFactory, redisTemplate.getConnectionFactory());
    }

    @Test
    void redisTemplate_ShouldConfigureSerializers() {
        // Given
        RedisConnectionFactory mockConnectionFactory = mock(RedisConnectionFactory.class);

        // When
        RedisTemplate<String, Object> redisTemplate = redisConfig.redisTemplate(mockConnectionFactory);

        // Then
        assertNotNull(redisTemplate);
        assertTrue(redisTemplate.getKeySerializer() instanceof StringRedisSerializer);
        assertTrue(redisTemplate.getHashKeySerializer() instanceof StringRedisSerializer);
        assertTrue(redisTemplate.getValueSerializer() instanceof GenericJackson2JsonRedisSerializer);
        assertTrue(redisTemplate.getHashValueSerializer() instanceof GenericJackson2JsonRedisSerializer);
    }

    @Test
    void redisTemplate_ShouldBeProperlyInitialized() {
        // Given
        RedisConnectionFactory mockConnectionFactory = mock(RedisConnectionFactory.class);

        // When
        RedisTemplate<String, Object> redisTemplate = redisConfig.redisTemplate(mockConnectionFactory);

        // Then
        assertNotNull(redisTemplate);
        assertNotNull(redisTemplate.getKeySerializer());
        assertNotNull(redisTemplate.getValueSerializer());
        assertNotNull(redisTemplate.getHashKeySerializer());
        assertNotNull(redisTemplate.getHashValueSerializer());
    }
}
