package com.vn.caro_game.configs;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Test configuration to disable WebSocket configuration for testing
 * This replaces the main WebSocketConfig to avoid Redis dependencies
 */
@TestConfiguration
@Profile("test")
public class TestWebSocketConfig {
    
    @Bean
    @Primary
    public WebSocketConfigurer testWebSocketConfigurer() {
        return new WebSocketConfigurer() {
            @Override
            public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
                // Do nothing - disable WebSocket in tests
            }
        };
    }
}
