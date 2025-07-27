package com.vn.caro_game.integrations.jwt;

import com.vn.caro_game.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private JwtServiceImpl jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        jwtService = new JwtServiceImpl(redisTemplate);
        
        // Set JWT configuration using reflection
        ReflectionTestUtils.setField(jwtService, "secretKey", "mySecretKeyForTestingPurposesOnly");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L); // 7 days

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
    }

    @Test
    @DisplayName("Should generate access token successfully")
    void shouldGenerateAccessTokenSuccessfully() {
        // When
        String token = jwtService.generateAccessToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("Should generate refresh token successfully")
    void shouldGenerateRefreshTokenSuccessfully() {
        // When
        String token = jwtService.generateRefreshToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("Should extract email from token")
    void shouldExtractEmailFromToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        String email = jwtService.extractEmail(token);

        // Then
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should extract user ID from token")
    void shouldExtractUserIdFromToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        Long userId = jwtService.extractUserId(token);

        // Then
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should validate token expiration")
    void shouldValidateTokenExpiration() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Should invalidate token")
    void shouldInvalidateToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        jwtService.invalidateToken(token);

        // Then
        verify(valueOperations).set(
            eq("blacklist:" + token),
            eq("blacklisted"),
            anyLong(),
            eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("Should check if token is blacklisted")
    void shouldCheckIfTokenIsBlacklisted() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        when(redisTemplate.hasKey("blacklist:" + token)).thenReturn(true);

        // When
        boolean isBlacklisted = jwtService.isTokenBlacklisted(token);

        // Then
        assertThat(isBlacklisted).isTrue();
    }

    @Test
    @DisplayName("Should return false for non-blacklisted token")
    void shouldReturnFalseForNonBlacklistedToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        when(redisTemplate.hasKey("blacklist:" + token)).thenReturn(false);

        // When
        boolean isBlacklisted = jwtService.isTokenBlacklisted(token);

        // Then
        assertThat(isBlacklisted).isFalse();
    }
}
