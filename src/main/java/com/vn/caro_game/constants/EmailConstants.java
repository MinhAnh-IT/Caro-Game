package com.vn.caro_game.constants;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class EmailConstants {
    
    // Template Placeholders
    public static final String PLACEHOLDER_USERNAME = "{{USERNAME}}";
    public static final String PLACEHOLDER_OTP = "{{OTP}}";
    public static final String PLACEHOLDER_DATE = "{{DATE}}";
    public static final String PLACEHOLDER_TIME = "{{TIME}}";
    
    // Email Properties
    public static final String SMTP_AUTH_PROPERTY = "mail.smtp.auth";
    public static final String SMTP_STARTTLS_ENABLE_PROPERTY = "mail.smtp.starttls.enable";
    public static final String SMTP_STARTTLS_REQUIRED_PROPERTY = "mail.smtp.starttls.required";
    
    // Default Values for Email Properties
    public static final String SMTP_AUTH_VALUE = "true";
    public static final String SMTP_STARTTLS_ENABLE_VALUE = "true";
    public static final String SMTP_STARTTLS_REQUIRED_VALUE = "true";
    
    // Email Template Names
    public static final String OTP_EMAIL_TEMPLATE = "otp-template.html";
    public static final String WELCOME_EMAIL_TEMPLATE = "welcome-template.html";
    public static final String PASSWORD_CHANGED_EMAIL_TEMPLATE = "password-changed-template.html";
    
    // Email Subjects
    public static final String WELCOME_EMAIL_SUBJECT = "Welcome to Caro Game! 🎮";
    public static final String PASSWORD_RESET_SUBJECT = "Password Reset - Caro Game";
    public static final String PASSWORD_CHANGE_VERIFICATION_SUBJECT = "Password Change Verification - Caro Game";
    public static final String PASSWORD_CHANGED_SUBJECT = "Password Changed Successfully - Caro Game 🔐";
}
