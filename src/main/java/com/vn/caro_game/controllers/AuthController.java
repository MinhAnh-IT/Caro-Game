package com.vn.caro_game.controllers;

import com.vn.caro_game.controllers.base.BaseController;
import com.vn.caro_game.controllers.interfaces.AuthControllerInterface;
import com.vn.caro_game.dtos.request.*;
import com.vn.caro_game.dtos.response.ApiResponse;
import com.vn.caro_game.dtos.response.AuthResponse;
import com.vn.caro_game.dtos.response.UserResponse;
import com.vn.caro_game.services.impl.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * Handles all authentication-related operations following clean architecture principles.
 * This controller provides endpoints for user registration, login, password management,
 * and token operations with comprehensive validation and error handling.
 * 
 * Features:
 * - Username-based authentication (modern approach)
 * - JWT token management (access + refresh tokens)
 * - Secure password reset with OTP verification
 * - Comprehensive API documentation with examples
 * - Consistent response format across all endpoints
 * 
 * @author MinhAnh-IT
 * @version 1.0
 * @since 2025-01-25
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(
    name = "Authentication", 
    description = "Complete authentication system with user management, security, and token operations"
)
public class AuthController extends BaseController implements AuthControllerInterface {
    
    AuthService authService;
    
    // ================================
    // USER REGISTRATION
    // ================================
    
    @PostMapping("/register")
    @Operation(
        summary = "Register new user account",
        description = """
            Register a new user account with comprehensive validation.
            
            **Features:**
            - Unique username and email validation
            - Strong password requirements
            - Immediate response with user data (excluding sensitive info)
            
            **Validation Rules:**
            - Username: 3-50 characters, alphanumeric and underscore only
            - Email: Valid email format, must be unique
            - Password: Minimum 8 characters with complexity requirements
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Successful Registration",
                    summary = "New user created successfully",
                    value = """
                        {
                            "statusCode": 201,
                            "message": "User registered successfully",
                            "success": true,
                            "data": {
                                "id": 1,
                                "username": "john_doe",
                                "email": "john@example.com",
                                "createdAt": "2025-01-25T10:30:00Z",
                                "avatarUrl": null
                            }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad request - validation failed or user already exists",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Username Exists",
                        summary = "Username already taken",
                        value = """
                            {
                                "statusCode": 400,
                                "message": "Username already exists",
                                "success": false,
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Validation Error",
                        summary = "Input validation failed",
                        value = """
                            {
                                "statusCode": 400,
                                "message": "Validation failed",
                                "success": false,
                                "data": {
                                    "username": "Username must be between 3 and 50 characters",
                                    "email": "Email format is invalid",
                                    "password": "Password must be at least 8 characters"
                                }
                            }
                            """
                    )
                }
            )
        )
    })
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Parameter(
                description = "User registration information",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserCreation.class),
                    examples = @ExampleObject(
                        name = "Registration Request",
                        summary = "Complete user registration data",
                        value = """
                            {
                                "username": "john_doe",
                                "email": "john@example.com",
                                "password": "SecurePassword123"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody UserCreation userCreation) {
        
        UserResponse userResponse = authService.register(userCreation);
        
        return created(userResponse, "User registered successfully");
    }
    
    // ================================
    // USER AUTHENTICATION
    // ================================
    
    @PostMapping("/login")
    @Operation(
        summary = "User authentication",
        description = """
            Authenticate user with username and password.
            
            **Features:**
            - Username-based login (modern security practice)
            - JWT token generation (access + refresh)
            - Secure password validation with BCrypt
            - Comprehensive token information in response
            
            **Security:**
            - Rate limiting: 5 attempts per 15 minutes
            - Account lockout after repeated failures
            - Secure session management
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Authentication successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Successful Login",
                    summary = "User authenticated successfully",
                    value = """
                        {
                            "statusCode": 200,
                            "message": "Login successful",
                            "success": true,
                            "data": {
                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huX2RvZSIsInVzZXJuYW1lIjoiam9obl9kb2UiLCJlbWFpbCI6ImpvaG5AZXhhbXBsZS5jb20iLCJpYXQiOjE2NDI2ODAwMDAsImV4cCI6MTY0MjY4MzYwMH0.signature",
                                "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huX2RvZSIsInR5cCI6InJlZnJlc2giLCJpYXQiOjE2NDI2ODAwMDAsImV4cCI6MTY0MzI4NDgwMH0.signature",
                                "tokenType": "Bearer",
                                "expiresIn": 3600,
                                "user": {
                                    "id": 1,
                                    "username": "john_doe",
                                    "email": "john@example.com",
                                    "createdAt": "2025-01-25T10:30:00Z"
                                }
                            }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication failed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Invalid Credentials",
                    summary = "Username or password incorrect",
                    value = """
                        {
                            "statusCode": 401,
                            "message": "Invalid username or password",
                            "success": false,
                            "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "423",
            description = "Account locked",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Account Locked",
                    summary = "Too many failed attempts",
                    value = """
                        {
                            "statusCode": 423,
                            "message": "Account temporarily locked due to multiple failed login attempts",
                            "success": false,
                            "data": null
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Parameter(
                description = "Login credentials with username and password",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(
                        name = "Login Request",
                        summary = "User authentication credentials",
                        value = """
                            {
                                "username": "john_doe",
                                "password": "SecurePassword123"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody LoginRequest loginRequest) {
        
        AuthResponse authResponse = authService.login(loginRequest);
        
        return success(authResponse, "Login successful");
    }
    
    // ================================
    // PASSWORD MANAGEMENT
    // ================================
    
    @PostMapping("/forgot-password")
    @Operation(
        summary = "Request password reset",
        description = """
            Request password reset OTP via email.
            
            **Features:**
            - Email validation before OTP generation
            - Rate limiting: 3 requests per 15 minutes per email
            - Secure OTP generation with expiration
            - Professional email template with instructions
            
            **Security:**
            - OTP expires in 15 minutes
            - Maximum 3 OTP requests per email per hour
            - Email validation to prevent spam
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Password reset OTP sent successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "OTP Sent",
                    summary = "Reset OTP sent to email",
                    value = """
                        {
                            "statusCode": 200,
                            "message": "Password reset OTP sent to your email",
                            "success": true,
                            "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Email not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Email Not Found",
                    summary = "No account with this email",
                    value = """
                        {
                            "statusCode": 404,
                            "message": "No account found with this email address",
                            "success": false,
                            "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "429",
            description = "Too many requests",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Rate Limited",
                    summary = "Too many OTP requests",
                    value = """
                        {
                            "statusCode": 429,
                            "message": "Too many OTP requests. Please try again later.",
                            "success": false,
                            "data": null
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Parameter(
                description = "Email address for password reset",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ForgotPasswordRequest.class),
                    examples = @ExampleObject(
                        name = "Forgot Password Request",
                        summary = "Email for password reset",
                        value = """
                            {
                                "email": "john@example.com"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        
        authService.forgotPassword(forgotPasswordRequest);
        
        return success("Password reset OTP sent to your email");
    }

    @PostMapping("/reset-password")
    @Operation(
        summary = "Reset password with OTP",
        description = """
            Reset user password using OTP verification.
            
            **Features:**
            - OTP validation with expiration check
            - Strong password requirements validation
            - Secure password hashing with BCrypt
            - Automatic OTP cleanup after successful reset
            
            **Security:**
            - OTP must be valid and not expired
            - New password must meet complexity requirements
            - Old password is securely overwritten
            - User receives confirmation email
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Password reset successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Password Reset Success",
                    summary = "Password updated successfully",
                    value = """
                        {
                            "statusCode": 200,
                            "message": "Password reset successfully",
                            "success": true,
                            "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid or expired OTP",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Invalid OTP",
                    summary = "OTP validation failed",
                    value = """
                        {
                            "statusCode": 400,
                            "message": "Invalid or expired OTP",
                            "success": false,
                            "data": null
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Parameter(
                description = "Password reset information with OTP and new password",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResetPasswordRequest.class),
                    examples = @ExampleObject(
                        name = "Reset Password Request",
                        summary = "OTP and new password",
                        value = """
                            {
                                "email": "john@example.com",
                                "otp": "123456",
                                "newPassword": "NewSecurePassword123"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        
        authService.resetPassword(resetPasswordRequest);
        
        return success("Password reset successfully");
    }

    // ================================
    // AUTHENTICATED USER OPERATIONS
    // ================================
    
    @PostMapping("/request-change-password-otp")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Request OTP for password change",
        description = """
            Request OTP for changing current password (authenticated users only).
            
            **Features:**
            - Requires valid JWT authentication
            - Email extracted from authenticated user context
            - Rate limiting to prevent abuse
            - Secure OTP generation and delivery
            
            **Security:**
            - User must be authenticated with valid JWT
            - OTP expires in 10 minutes for security
            - Maximum 3 requests per user per hour
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "202",
            description = "Change password OTP sent",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "OTP Request Accepted",
                    summary = "OTP generation initiated",
                    value = """
                        {
                            "statusCode": 202,
                            "message": "Change password OTP sent to your email",
                            "success": true,
                            "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Unauthorized",
                    summary = "Authentication required",
                    value = """
                        {
                            "statusCode": 401,
                            "message": "Authentication required",
                            "success": false,
                            "data": null
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<Void>> requestChangePasswordOtp() {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        authService.requestChangePasswordOtp(email);
        
        return accepted("Change password OTP sent to your email");
    }

    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Change current password",
        description = """
            Change current password using current password and OTP verification.
            
            **Features:**
            - Requires authentication with valid JWT
            - Current password verification for security
            - OTP validation for additional security layer
            - Strong new password requirements
            
            **Security:**
            - Must provide correct current password
            - Must provide valid OTP from email
            - New password must meet complexity requirements
            - Automatic JWT invalidation for security
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Password changed successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Password Changed",
                    summary = "Password updated successfully",
                    value = """
                        {
                            "statusCode": 200,
                            "message": "Password changed successfully",
                            "success": true,
                            "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid current password or OTP",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Invalid Input",
                    summary = "Validation failed",
                    value = """
                        {
                            "statusCode": 400,
                            "message": "Current password is incorrect or OTP is invalid",
                            "success": false,
                            "data": null
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Parameter(
                description = "Password change information",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChangePasswordRequest.class),
                    examples = @ExampleObject(
                        name = "Change Password Request",
                        summary = "Current password, OTP and new password",
                        value = """
                            {
                                "currentPassword": "CurrentPassword123",
                                "newPassword": "NewSecurePassword123",
                                "otp": "123456"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        authService.changePassword(changePasswordRequest, email);
        
        return success("Password changed successfully");
    }

    // ================================
    // TOKEN MANAGEMENT
    // ================================
    
    @PostMapping("/refresh-token")
    @Operation(
        summary = "Refresh JWT access token",
        description = """
            Refresh expired access token using valid refresh token.
            
            **Features:**
            - Exchange valid refresh token for new access token
            - Automatic refresh token rotation for security
            - User activity tracking and audit logging
            - Token blacklist management for revoked tokens
            
            **Security:**
            - Refresh token validation with expiration check
            - Automatic old token revocation
            - Rate limiting to prevent token abuse
            - User session validation
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Token Refreshed",
                    summary = "New tokens generated",
                    value = """
                        {
                            "statusCode": 200,
                            "message": "Token refreshed successfully",
                            "success": true,
                            "data": {
                                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
                                "tokenType": "Bearer",
                                "expiresIn": 3600
                            }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid or expired refresh token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Invalid Refresh Token",
                    summary = "Token validation failed",
                    value = """
                        {
                            "statusCode": 401,
                            "message": "Invalid or expired refresh token",
                            "success": false,
                            "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "423",
            description = "Account locked or deactivated",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Account Locked",
                    summary = "Account status preventing refresh",
                    value = """
                        {
                            "statusCode": 423,
                            "message": "Account is locked or deactivated",
                            "success": false,
                            "data": null
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Parameter(
                description = "Refresh token request",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RefreshTokenRequest.class),
                    examples = @ExampleObject(
                        name = "Refresh Token Request",
                        summary = "Valid refresh token",
                        value = """
                            {
                                "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIi..."
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        
        AuthResponse authResponse = authService.refreshToken(refreshTokenRequest);
        
        return success(authResponse, "Token refreshed successfully");
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "User logout",
        description = """
            Logout authenticated user and invalidate tokens.
            
            **Features:**
            - Token blacklisting for immediate invalidation
            - User session termination
            - Activity logging for security audit
            - Cross-device logout support
            
            **Security:**
            - Immediate token revocation
            - Session cleanup in Redis cache
            - Security event logging
            - Protection against token reuse
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Logout successful",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Logout Success",
                    summary = "User logged out successfully",
                    value = """
                        {
                            "statusCode": 200,
                            "message": "Logout successful",
                            "success": true,
                            "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Unauthorized",
                    summary = "Invalid authentication",
                    value = """
                        {
                            "statusCode": 401,
                            "message": "Authentication required",
                            "success": false,
                            "data": null
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(
                description = "Authorization header with Bearer token",
                required = true,
                schema = @Schema(type = "string", format = "bearer")
            )
            @RequestHeader("Authorization") String accessToken) {
        
        // Extract token from "Bearer " prefix
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        authService.logout(token);
        
        return success("Logout successful");
    }
}
