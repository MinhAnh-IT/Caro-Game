package com.vn.caro_game.integrations.redis;

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
@DisplayName("OtpService Tests")
class OtpServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private OtpServiceImpl otpService;

    private final String testEmail = "test@example.com";
    private final String testOtpType = "RESET_PASSWORD";
    private final String testOtp = "123456";

    @BeforeEach
    void setUp() {
        // Setup mocking for Redis operations with lenient stubbing
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        otpService = new OtpServiceImpl(redisTemplate);
        
        // Set test values using reflection
        ReflectionTestUtils.setField(otpService, "otpExpirationMinutes", 5);
        ReflectionTestUtils.setField(otpService, "maxOtpAttempts", 3);
    }

    @Test
    @DisplayName("Should generate 6-digit OTP")
    void shouldGenerate6DigitOtp() {
        // When
        String otp = otpService.generateOtp();

        // Then
        assertThat(otp).isNotNull();
        assertThat(otp).hasSize(6);
        assertThat(otp).matches("\\d{6}");
        
        // Verify it's in the valid range
        int otpValue = Integer.parseInt(otp);
        assertThat(otpValue).isBetween(100000, 999999);
    }

    @Test
    @DisplayName("Should generate different OTPs on subsequent calls")
    void shouldGenerateDifferentOtpsOnSubsequentCalls() {
        // When
        String otp1 = otpService.generateOtp();
        String otp2 = otpService.generateOtp();

        // Then (very high probability they will be different)
        assertThat(otp1).isNotEqualTo(otp2);
    }

    @Test
    @DisplayName("Should store OTP with expiration")
    void shouldStoreOtpWithExpiration() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // When
        otpService.storeOtp(testEmail, testOtp, testOtpType);

        // Then
        verify(valueOperations).set(
                eq("otp:" + testOtpType + ":" + testEmail),
                eq(testOtp),
                eq(5L), // Use Long instead of Integer
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("Should validate OTP when correct")
    void shouldValidateOtpWhenCorrect() {
        // Given
        String key = "otp:" + testOtpType + ":" + testEmail;
        when(valueOperations.get(key)).thenReturn(testOtp);

        // When
        boolean isValid = otpService.validateOtp(testEmail, testOtp, testOtpType);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should not validate OTP when incorrect")
    void shouldNotValidateOtpWhenIncorrect() {
        // Given
        String key = "otp:" + testOtpType + ":" + testEmail;
        when(valueOperations.get(key)).thenReturn(testOtp);

        // When
        boolean isValid = otpService.validateOtp(testEmail, "wrong-otp", testOtpType);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should not validate OTP when not stored")
    void shouldNotValidateOtpWhenNotStored() {
        // Given
        String key = "otp:" + testOtpType + ":" + testEmail;
        when(valueOperations.get(key)).thenReturn(null);

        // When
        boolean isValid = otpService.validateOtp(testEmail, testOtp, testOtpType);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should remove OTP and reset attempts")
    void shouldRemoveOtpAndResetAttempts() {
        // When
        otpService.removeOtp(testEmail, testOtpType);

        // Then
        verify(redisTemplate).delete("otp:" + testOtpType + ":" + testEmail);
        verify(redisTemplate).delete("otp_attempts:" + testOtpType + ":" + testEmail);
    }

    @Test
    @DisplayName("Should get OTP attempts when exists")
    void shouldGetOtpAttemptsWhenExists() {
        // Given
        String key = "otp_attempts:" + testOtpType + ":" + testEmail;
        when(valueOperations.get(key)).thenReturn(2);

        // When
        int attempts = otpService.getOtpAttempts(testEmail, testOtpType);

        // Then
        assertThat(attempts).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return 0 attempts when not exists")
    void shouldReturn0AttemptsWhenNotExists() {
        // Given
        String key = "otp_attempts:" + testOtpType + ":" + testEmail;
        when(valueOperations.get(key)).thenReturn(null);

        // When
        int attempts = otpService.getOtpAttempts(testEmail, testOtpType);

        // Then
        assertThat(attempts).isEqualTo(0);
    }

    @Test
    @DisplayName("Should increment OTP attempts from 0")
    void shouldIncrementOtpAttemptsFrom0() {
        // Given
        String key = "otp_attempts:" + testOtpType + ":" + testEmail;
        when(valueOperations.get(key)).thenReturn(null);

        // When
        otpService.incrementOtpAttempts(testEmail, testOtpType);

        // Then
        verify(valueOperations).set(
                eq(key),
                eq(1),
                eq(5L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("Should increment OTP attempts from existing value")
    void shouldIncrementOtpAttemptsFromExistingValue() {
        // Given
        String key = "otp_attempts:" + testOtpType + ":" + testEmail;
        when(valueOperations.get(key)).thenReturn(2);

        // When
        otpService.incrementOtpAttempts(testEmail, testOtpType);

        // Then
        verify(valueOperations).set(
                eq(key),
                eq(3),
                eq(5L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("Should reset OTP attempts")
    void shouldResetOtpAttempts() {
        // When
        otpService.resetOtpAttempts(testEmail, testOtpType);

        // Then
        verify(redisTemplate).delete("otp_attempts:" + testOtpType + ":" + testEmail);
    }

    @Test
    @DisplayName("Should check if OTP attempts exceeded when below limit")
    void shouldCheckIfOtpAttemptsExceededWhenBelowLimit() {
        // Given
        String key = "otp_attempts:" + testOtpType + ":" + testEmail;
        when(valueOperations.get(key)).thenReturn(2);

        // When
        boolean isExceeded = otpService.isOtpAttemptsExceeded(testEmail, testOtpType);

        // Then
        assertThat(isExceeded).isFalse();
    }

    @Test
    @DisplayName("Should check if OTP attempts exceeded when at limit")
    void shouldCheckIfOtpAttemptsExceededWhenAtLimit() {
        // Given
        String key = "otp_attempts:" + testOtpType + ":" + testEmail;
        when(valueOperations.get(key)).thenReturn(3);

        // When
        boolean isExceeded = otpService.isOtpAttemptsExceeded(testEmail, testOtpType);

        // Then
        assertThat(isExceeded).isTrue();
    }

    @Test
    @DisplayName("Should check if OTP attempts exceeded when above limit")
    void shouldCheckIfOtpAttemptsExceededWhenAboveLimit() {
        // Given
        String key = "otp_attempts:" + testOtpType + ":" + testEmail;
        when(valueOperations.get(key)).thenReturn(5);

        // When
        boolean isExceeded = otpService.isOtpAttemptsExceeded(testEmail, testOtpType);

        // Then
        assertThat(isExceeded).isTrue();
    }

    @Test
    @DisplayName("Should build correct OTP key")
    void shouldBuildCorrectOtpKey() {
        // When
        otpService.storeOtp(testEmail, testOtp, testOtpType);

        // Then
        verify(valueOperations).set(
                eq("otp:" + testOtpType + ":" + testEmail),
                anyString(),
                anyLong(),
                any(TimeUnit.class)
        );
    }

    @Test
    @DisplayName("Should build correct attempts key")
    void shouldBuildCorrectAttemptsKey() {
        // When
        otpService.incrementOtpAttempts(testEmail, testOtpType);

        // Then
        verify(valueOperations).set(
                eq("otp_attempts:" + testOtpType + ":" + testEmail),
                anyInt(),
                anyLong(),
                any(TimeUnit.class)
        );
    }
}
