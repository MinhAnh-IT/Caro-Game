package com.vn.caro_game;

import com.vn.caro_game.configs.TestWebSocketConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base test class that configures all necessary test configurations
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestWebSocketConfig.class})
public abstract class BaseTestConfiguration {
    // This class provides common test configuration for all tests
}
