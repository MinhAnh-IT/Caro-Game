package com.vn.caro_game.controllers.interfaces;

import com.vn.caro_game.dtos.request.*;
import com.vn.caro_game.dtos.response.ApiResponse;
import com.vn.caro_game.dtos.response.AuthResponse;
import com.vn.caro_game.dtos.response.UserResponse;
import org.springframework.http.ResponseEntity;

/**
 * Authentication Controller Interface
 * Defines contract for authentication operations following clean architecture
 */
public interface AuthControllerInterface {
    
    /**
     * Register a new user account
     */
    ResponseEntity<ApiResponse<UserResponse>> register(UserCreation userCreation);
    
    /**
     * Authenticate user with username and password
     */
    ResponseEntity<ApiResponse<AuthResponse>> login(LoginRequest loginRequest);
    
    /**
     * Request password reset OTP
     */
    ResponseEntity<ApiResponse<Void>> forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    
    /**
     * Reset password using OTP
     */
    ResponseEntity<ApiResponse<Void>> resetPassword(ResetPasswordRequest resetPasswordRequest);
    
    /**
     * Request OTP for password change
     */
    ResponseEntity<ApiResponse<Void>> requestChangePasswordOtp();
    
    /**
     * Change password using current password and OTP
     */
    ResponseEntity<ApiResponse<Void>> changePassword(ChangePasswordRequest changePasswordRequest);
    
    /**
     * Refresh access token using refresh token
     */
    ResponseEntity<ApiResponse<AuthResponse>> refreshToken(RefreshTokenRequest refreshTokenRequest);
    
    /**
     * Logout user and invalidate tokens
     */
    ResponseEntity<ApiResponse<Void>> logout(String accessToken);
}
