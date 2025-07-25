package com.vn.caro_game.integrations.redis;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OtpServiceImpl implements OtpService {
    
    final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${otp.expiration-minutes:5}")
    int otpExpirationMinutes;
    
    @Value("${otp.max-attempts:3}")
    int maxOtpAttempts;
    
    static final String OTP_PREFIX = "otp:";
    static final String ATTEMPTS_PREFIX = "otp_attempts:";
    final SecureRandom random = new SecureRandom();
    
    public OtpServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    
    @Override
    public void storeOtp(String email, String otp, String type) {
        String key = buildOtpKey(email, type);
        redisTemplate.opsForValue().set(key, otp, otpExpirationMinutes, TimeUnit.MINUTES);
    }
    
    @Override
    public boolean validateOtp(String email, String otp, String type) {
        String key = buildOtpKey(email, type);
        String storedOtp = (String) redisTemplate.opsForValue().get(key);
        return otp.equals(storedOtp);
    }
    
    @Override
    public void removeOtp(String email, String type) {
        String key = buildOtpKey(email, type);
        redisTemplate.delete(key);
        resetOtpAttempts(email, type);
    }
    
    @Override
    public int getOtpAttempts(String email, String type) {
        String key = buildAttemptsKey(email, type);
        Object attempts = redisTemplate.opsForValue().get(key);
        return attempts != null ? (Integer) attempts : 0;
    }
    
    @Override
    public void incrementOtpAttempts(String email, String type) {
        String key = buildAttemptsKey(email, type);
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
        if (attempts == null) {
            attempts = 0;
        }
        redisTemplate.opsForValue().set(key, attempts + 1, otpExpirationMinutes, TimeUnit.MINUTES);
    }
    
    @Override
    public void resetOtpAttempts(String email, String type) {
        String key = buildAttemptsKey(email, type);
        redisTemplate.delete(key);
    }
    
    @Override
    public boolean isOtpAttemptsExceeded(String email, String type) {
        return getOtpAttempts(email, type) >= maxOtpAttempts;
    }
    
    private String buildOtpKey(String email, String type) {
        return OTP_PREFIX + type + ":" + email;
    }
    
    private String buildAttemptsKey(String email, String type) {
        return ATTEMPTS_PREFIX + type + ":" + email;
    }
}
