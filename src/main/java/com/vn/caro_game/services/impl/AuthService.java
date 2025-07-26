package com.vn.caro_game.services.impl;

import com.vn.caro_game.constants.ApplicationConstants;
import com.vn.caro_game.constants.EmailConstants;
import com.vn.caro_game.constants.MessageConstants;
import com.vn.caro_game.dtos.request.*;
import com.vn.caro_game.dtos.response.AuthResponse;
import com.vn.caro_game.dtos.response.UserResponse;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.StatusCode;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.integrations.email.EmailService;
import com.vn.caro_game.integrations.jwt.JwtService;
import com.vn.caro_game.integrations.redis.OtpService;
import com.vn.caro_game.mappers.UserMapper;
import com.vn.caro_game.repositories.UserRepository;
import com.vn.caro_game.services.interfaces.AuthServiceInterface;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class AuthService implements AuthServiceInterface {
    
    final UserRepository userRepository;
    final UserMapper userMapper;
    final PasswordEncoder passwordEncoder;
    final JwtService jwtService;
    final EmailService emailService;
    final OtpService otpService;
    
    @Value("${otp.max-attempts:3}")
    int maxOtpAttempts;

    
    static final String CHANGE_PASSWORD_OTP = MessageConstants.CHANGE_PASSWORD_OTP_TYPE;
    static final String RESET_PASSWORD_OTP = MessageConstants.RESET_PASSWORD_OTP_TYPE;
    
    @Transactional
    public UserResponse register(UserCreation userCreation) {
        log.info("Starting user registration for email: {}", userCreation.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(userCreation.getEmail())) {
            throw new CustomException(StatusCode.EMAIL_ALREADY_EXISTS);
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(userCreation.getUsername())) {
            throw new CustomException(StatusCode.USERNAME_ALREADY_EXISTS);
        }
        
        // Create user entity
        User user = userMapper.toEntity(userCreation);
        user.setPassword(passwordEncoder.encode(userCreation.getPassword()));
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Send welcome email
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        } catch (Exception e) {
            log.warn("Failed to send welcome email to: {}", user.getEmail(), e);
        }
        
        log.info("User registered successfully with email: {}", savedUser.getEmail());
        return userMapper.toResponse(savedUser);
    }
    
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for username: {}", loginRequest.getUsername());
        
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new CustomException(StatusCode.INVALID_CREDENTIALS));
        
        // Validate password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new CustomException(StatusCode.INVALID_CREDENTIALS);
        }
        
        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        log.info("User logged in successfully: {}", user.getUsername());
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(MessageConstants.TOKEN_TYPE_BEARER)
                .expiresIn(ApplicationConstants.DEFAULT_ACCESS_TOKEN_EXPIRATION)
                .user(userMapper.toResponse(user))
                .build();
    }
    
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());
        
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(StatusCode.EMAIL_NOT_FOUND));
        
        // Check OTP attempts limit
        if (otpService.isOtpAttemptsExceeded(request.getEmail(), RESET_PASSWORD_OTP)) {
            throw new CustomException(StatusCode.OTP_ATTEMPTS_EXCEEDED);
        }
        
        // Generate and send OTP
        String otp = otpService.generateOtp();
        otpService.storeOtp(request.getEmail(), otp, RESET_PASSWORD_OTP);
        emailService.sendOtpEmail(request.getEmail(), otp, EmailConstants.PASSWORD_RESET_SUBJECT);
        
        log.info("Reset password OTP sent to: {}", request.getEmail());
    }
    
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Reset password attempt for email: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(StatusCode.EMAIL_NOT_FOUND));
        
        // Validate OTP
        if (!otpService.validateOtp(request.getEmail(), request.getOtp(), RESET_PASSWORD_OTP)) {
            otpService.incrementOtpAttempts(request.getEmail(), RESET_PASSWORD_OTP);
            throw new CustomException(StatusCode.INVALID_OTP);
        }
        
        // Update password
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        userRepository.updatePassword(request.getEmail(), encodedPassword);
        
        // Remove OTP
        otpService.removeOtp(request.getEmail(), RESET_PASSWORD_OTP);
        
        // Send password change notification
        emailService.sendPasswordChangeNotification(request.getEmail(), user.getUsername());
        
        log.info("Password reset successfully for: {}", request.getEmail());
    }
    
    public void requestChangePasswordOtp(String email) {
        log.info("Change password OTP request for email: {}", email);
        
        userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(StatusCode.USER_NOT_FOUND));
        
        // Check OTP attempts limit
        if (otpService.isOtpAttemptsExceeded(email, CHANGE_PASSWORD_OTP)) {
            throw new CustomException(StatusCode.OTP_ATTEMPTS_EXCEEDED);
        }
        
        // Generate and send OTP
        String otp = otpService.generateOtp();
        otpService.storeOtp(email, otp, CHANGE_PASSWORD_OTP);
        emailService.sendOtpEmail(email, otp, EmailConstants.PASSWORD_CHANGE_VERIFICATION_SUBJECT);
        
        log.info("Change password OTP sent to: {}", email);
    }
    
    @Transactional
    public void changePassword(ChangePasswordRequest request, String email) {
        log.info("Change password attempt for email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(StatusCode.USER_NOT_FOUND));
        
        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(StatusCode.CURRENT_PASSWORD_INCORRECT);
        }
        
        // Validate OTP
        if (!otpService.validateOtp(email, request.getOtp(), CHANGE_PASSWORD_OTP)) {
            otpService.incrementOtpAttempts(email, CHANGE_PASSWORD_OTP);
            throw new CustomException(StatusCode.INVALID_OTP);
        }
        
        // Update password
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        userRepository.updatePassword(email, encodedPassword);
        
        // Remove OTP
        otpService.removeOtp(email, CHANGE_PASSWORD_OTP);
        
        // Send password change notification
        emailService.sendPasswordChangeNotification(email, user.getUsername());
        
        log.info("Password changed successfully for: {}", email);
    }
    
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refresh token request");
        
        String refreshToken = request.getRefreshToken();
        
        // Validate token
        if (jwtService.isTokenExpired(refreshToken) || jwtService.isTokenBlacklisted(refreshToken)) {
            throw new CustomException(StatusCode.INVALID_REFRESH_TOKEN);
        }
        
        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(StatusCode.USER_NOT_FOUND));
        
        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        
        // Blacklist old refresh token
        jwtService.invalidateToken(refreshToken);
        
        log.info("Tokens refreshed successfully for user: {}", email);
        
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType(MessageConstants.TOKEN_TYPE_BEARER)
                .expiresIn(ApplicationConstants.DEFAULT_ACCESS_TOKEN_EXPIRATION)
                .user(userMapper.toResponse(user))
                .build();
    }
    
    public void logout(String token) {
        log.info("Logout request");
        
        // Blacklist token
        jwtService.invalidateToken(token);
        
        log.info("User logged out successfully");
    }
    
}
