package com.vn.caro_game.integrations.email;

public interface EmailService {
    void sendOtpEmail(String to, String otp, String subject);
    void sendWelcomeEmail(String to, String username);
    void sendPasswordChangeNotification(String to, String username);
}
