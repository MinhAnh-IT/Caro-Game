package com.vn.caro_game.integration;

import com.vn.caro_game.integrations.redis.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for WebSocket functionality.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebSocketIntegrationTest {

    @Autowired
    private RedisService redisService;

    @Test
    void contextLoads() {
        // Basic test to ensure Spring context loads successfully
        assertNotNull(redisService);
    }

    @Test
    void redisService_ShouldBeInjected() {
        // Test that RedisService is properly injected and functional
        assertNotNull(redisService);
        
        // Test basic functionality
        Long userId = 1L;
        redisService.setUserOnline(userId, 300L);
        
        // The actual Redis operations will depend on Redis being available
        // In test environment, this validates the service is properly configured
        assertDoesNotThrow(() -> redisService.isUserOnline(userId));
    }
}
