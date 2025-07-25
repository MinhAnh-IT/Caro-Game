package com.vn.caro_game.integrations.redis;

public interface OtpService {
    String generateOtp();
    void storeOtp(String email, String otp, String type);
    boolean validateOtp(String email, String otp, String type);
    void removeOtp(String email, String type);
    int getOtpAttempts(String email, String type);
    void incrementOtpAttempts(String email, String type);
    void resetOtpAttempts(String email, String type);
    boolean isOtpAttemptsExceeded(String email, String type);
}
