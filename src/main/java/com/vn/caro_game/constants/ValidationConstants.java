package com.vn.caro_game.constants;

/**
 * Constants for validation constraints and messages
 * Centralizes all validation rules to ensure consistency across the application
 */
public final class ValidationConstants {
    
    private ValidationConstants() {
        // Prevent instantiation
    }
    
    // Password validation
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 128;
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$";
    
    // Username validation
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 50;
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9._-]+$";
    
    // Email validation
    public static final int EMAIL_MAX_LENGTH = 254;
    
    // OTP validation
    public static final String OTP_PATTERN = "^[0-9]{6}$";
    
    // Validation messages
    public static final String PASSWORD_BLANK_MESSAGE = "Password cannot be blank";
    public static final String PASSWORD_SIZE_MESSAGE = "Password must be between " + PASSWORD_MIN_LENGTH + " and " + PASSWORD_MAX_LENGTH + " characters";
    public static final String PASSWORD_PATTERN_MESSAGE = "Password must contain at least one uppercase letter, one lowercase letter, and one digit";
    
    public static final String USERNAME_BLANK_MESSAGE = "Username cannot be blank";
    public static final String USERNAME_SIZE_MESSAGE = "Username must be between " + USERNAME_MIN_LENGTH + " and " + USERNAME_MAX_LENGTH + " characters";
    public static final String USERNAME_PATTERN_MESSAGE = "Username can only contain letters, numbers, dots, underscores, and hyphens";
    
    public static final String EMAIL_BLANK_MESSAGE = "Email cannot be blank";
    public static final String EMAIL_INVALID_MESSAGE = "Email format is invalid";
    public static final String EMAIL_SIZE_MESSAGE = "Email must not exceed " + EMAIL_MAX_LENGTH + " characters";
    
    public static final String OTP_BLANK_MESSAGE = "OTP cannot be blank";
    public static final String OTP_PATTERN_MESSAGE = "OTP must be 6 digits";
    
    public static final String CURRENT_PASSWORD_BLANK_MESSAGE = "Current password cannot be blank";
    public static final String NEW_PASSWORD_BLANK_MESSAGE = "New password cannot be blank";
}