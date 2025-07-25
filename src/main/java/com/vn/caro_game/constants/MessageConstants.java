package com.vn.caro_game.constants;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class MessageConstants {
    
    // Success Messages
    public static final String REGISTRATION_SUCCESSFUL = "Registration successful";
    public static final String LOGIN_SUCCESSFUL = "Login successful";
    public static final String OTP_SENT_EMAIL = "OTP has been sent to your email";
    public static final String PASSWORD_RESET_SUCCESSFUL = "Password has been reset successfully";
    public static final String PASSWORD_CHANGED_SUCCESSFUL = "Password has been changed successfully";
    public static final String TOKEN_REFRESHED_SUCCESSFUL = "Token refreshed successfully";
    public static final String LOGOUT_SUCCESSFUL = "Logout successful";
    
    // Generic Error Messages
    public static final String INVALID_INPUT_DATA = "Invalid input data";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred";
    
    // Error Codes
    public static final String VALIDATION_ERROR_CODE = "VALIDATION_ERROR";
    
    // HTTP Headers
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    
    // Token Types
    public static final String TOKEN_TYPE_BEARER = "Bearer";
    
    // Date/Time Formats
    public static final String DATE_FORMAT_PATTERN = "MMMM dd, yyyy";
    public static final String TIME_FORMAT_PATTERN = "HH:mm:ss";
    
    // Template Paths
    public static final String EMAIL_TEMPLATE_PATH = "templates/email/";
    
    // Charset
    public static final String CHARSET_UTF8 = "UTF-8";
    
    // OTP Constants
    public static final String CHANGE_PASSWORD_OTP_TYPE = "CHANGE_PASSWORD";
    public static final String RESET_PASSWORD_OTP_TYPE = "RESET_PASSWORD";
}
