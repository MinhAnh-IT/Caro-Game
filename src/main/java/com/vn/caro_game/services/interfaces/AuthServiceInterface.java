package com.vn.caro_game.services.interfaces;

import com.vn.caro_game.dtos.request.*;
import com.vn.caro_game.dtos.response.AuthResponse;
import com.vn.caro_game.dtos.response.UserResponse;

/**
 * Authentication Service Interface for user authentication and authorization operations.
 * 
 * <p>This interface defines the contract for authentication business logic following
 * clean architecture principles with clear separation of concerns.</p>
 * 
 * <h3>Core Operations:</h3>
 * <ul>
 *   <li><strong>Registration:</strong> New user account creation with validation</li>
 *   <li><strong>Authentication:</strong> Login with JWT token generation</li>
 *   <li><strong>OTP Management:</strong> Email verification and password reset</li>
 *   <li><strong>Token Management:</strong> JWT refresh and logout operations</li>
 * </ul>
 * 
 * <h3>Security Features:</h3>
 * <ul>
 *   <li>Email verification with OTP</li>
 *   <li>Password complexity validation</li>
 *   <li>JWT token blacklisting</li>
 *   <li>Rate limiting and security checks</li>
 * </ul>
 * 
 * @author Caro Game Team
 * @since 1.0.0
 * @see AuthService
 * @see UserResponse
 * @see AuthResponse
 */
public interface AuthServiceInterface {
    
    /**
     * Registers a new user account with email verification.
     * 
     * <p>Creates a new user account with the provided registration data.
     * Sends an OTP email for account verification.</p>
     * 
     * @param userCreation user registration data including username, email, and password
     * @return UserResponse containing the created user details
     * @throws RuntimeException if username/email already exists or validation fails
     */
    UserResponse register(UserCreation userCreation);
    
    /**
     * Authenticates user with username/email and password.
     * 
     * <p>Validates user credentials and generates JWT access/refresh tokens
     * for authenticated sessions.</p>
     * 
     * @param loginRequest login credentials (username/email and password)
     * @return AuthResponse containing JWT tokens and user information
     * @throws RuntimeException if credentials are invalid or account is not verified
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
