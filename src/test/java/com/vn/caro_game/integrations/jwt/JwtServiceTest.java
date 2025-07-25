package com.vn.caro_game.integrations.jwt;

import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.UserStatus;
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
        // Remove the unnecessary stubbing
        // when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        jwtService = new JwtServiceImpl(redisTemplate);
        
        // Set test values using reflection
        ReflectionTestUtils.setField(jwtService, "secretKey", "mySecretKeyForTestingPurposesWhichShouldBeAtLeast256BitsLong");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L); // 7 days

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setStatus(UserStatus.ONLINE);
    }

    @Test
    @DisplayName("Should generate access token with user claims")
    void shouldGenerateAccessTokenWithUserClaims() {
        // When
        String token = jwtService.generateAccessToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        // Verify token claims
        String extractedEmail = jwtService.extractEmail(token);
        Long extractedUserId = jwtService.extractUserId(token);
        
        assertThat(extractedEmail).isEqualTo(testUser.getEmail());
        assertThat(extractedUserId).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Should generate refresh token")
    void shouldGenerateRefreshToken() {
        // When
        String token = jwtService.generateRefreshToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        // Verify token email
        String extractedEmail = jwtService.extractEmail(token);
        assertThat(extractedEmail).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("Should extract email from token")
    void shouldExtractEmailFromToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        String extractedEmail = jwtService.extractEmail(token);

        // Then
        assertThat(extractedEmail).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("Should extract user ID from token")
    void shouldExtractUserIdFromToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        Long extractedUserId = jwtService.extractUserId(token);

        // Then
        assertThat(extractedUserId).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Should validate token when valid")
    void shouldValidateTokenWhenValid() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        // When
        boolean isValid = jwtService.isTokenValid(token, testUser.getEmail());

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should not validate token when email mismatch")
    void shouldNotValidateTokenWhenEmailMismatch() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        // Remove unnecessary stubbing
        // when(redisTemplate.hasKey(anyString())).thenReturn(false);

        // When
        boolean isValid = jwtService.isTokenValid(token, "different@example.com");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should not validate token when blacklisted")
    void shouldNotValidateTokenWhenBlacklisted() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        // When
        boolean isValid = jwtService.isTokenValid(token, testUser.getEmail());

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should check if token is not expired for valid token")
    void shouldCheckIfTokenIsNotExpiredForValidToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Should invalidate token by adding to blacklist")
    void shouldInvalidateTokenByAddingToBlacklist() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

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
    @DisplayName("Should check if token is not blacklisted")
    void shouldCheckIfTokenIsNotBlacklisted() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        when(redisTemplate.hasKey("blacklist:" + token)).thenReturn(false);

        // When
        boolean isBlacklisted = jwtService.isTokenBlacklisted(token);

        // Then
        assertThat(isBlacklisted).isFalse();
    }

    @Test
    @DisplayName("Should generate different tokens for same user")
    void shouldGenerateDifferentTokensForSameUser() throws InterruptedException {
        // When
        String token1 = jwtService.generateAccessToken(testUser);
        Thread.sleep(1000); // Wait 1 second to ensure different timestamps
        String token2 = jwtService.generateAccessToken(testUser);

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("Should generate different access and refresh tokens")
    void shouldGenerateDifferentAccessAndRefreshTokens() {
        // When
        String accessToken = jwtService.generateAccessToken(testUser);
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Then
        assertThat(accessToken).isNotEqualTo(refreshToken);
    }
}
