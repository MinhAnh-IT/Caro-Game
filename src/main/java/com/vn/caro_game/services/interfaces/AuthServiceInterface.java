package com.vn.caro_game.services.interfaces;

import com.vn.caro_game.dtos.request.*;
import com.vn.caro_game.dtos.response.AuthResponse;
import com.vn.caro_game.dtos.response.UserResponse;

/**
 * Authentication Service Interface
 * Defines contract for authentication business logic
 * Follows clean architecture principles with clear separation of concerns
 */
public interface AuthServiceInterface {
    
    /**
     * Register a new user
     * @param userCreation User registration data
     * @return UserResponse with user details
     */
    UserResponse register(UserCreation userCreation);
    
    /**
     * Authenticate user with username/password
     * @param loginRequest Login credentials
     * @return AuthResponse with tokens and user info
     */
    AuthResponse login(LoginRequest loginRequest);
    
    /**
     * Send password reset OTP to user email
     * @param forgotPasswordRequest Request with email
     */
    void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    
    /**
     * Reset password using OTP verification
     * @param resetPasswordRequest Request with email, OTP and new password
     */
    void resetPassword(ResetPasswordRequest resetPasswordRequest);
    
    /**
     * Send OTP for password change
     * @param email User email
     */
    void requestChangePasswordOtp(String email);
    
    /**
     * Change password using current password and OTP
     * @param changePasswordRequest Request with passwords and OTP
     * @param email User email
     */
    void changePassword(ChangePasswordRequest changePasswordRequest, String email);
    
    /**
     * Refresh access token
     * @param refreshTokenRequest Request with refresh token
     * @return AuthResponse with new tokens
     */
    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
    
    /**
     * Logout user and invalidate token
     * @param token Access token to invalidate
     */
    void logout(String token);
}
