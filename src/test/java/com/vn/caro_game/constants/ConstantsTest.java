package com.vn.caro_game.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Constants Tests")
class ConstantsTest {

    @Test
    @DisplayName("Should have correct HTTP status constants")
    void shouldHaveCorrectHttpStatusConstants() {
        assertThat(HttpStatusConstants.OK).isEqualTo(200);
        assertThat(HttpStatusConstants.BAD_REQUEST).isEqualTo(400);
        assertThat(HttpStatusConstants.UNAUTHORIZED).isEqualTo(401);
        assertThat(HttpStatusConstants.FORBIDDEN).isEqualTo(403);
        assertThat(HttpStatusConstants.NOT_FOUND).isEqualTo(404);
        assertThat(HttpStatusConstants.TOO_MANY_REQUESTS).isEqualTo(429);
        assertThat(HttpStatusConstants.INTERNAL_SERVER_ERROR).isEqualTo(500);
    }

    @Test
    @DisplayName("Should have correct application constants")
    void shouldHaveCorrectApplicationConstants() {
        assertThat(ApplicationConstants.DEFAULT_ACCESS_TOKEN_EXPIRATION).isEqualTo(3600L);
        assertThat(ApplicationConstants.BEARER_TOKEN_START_INDEX).isEqualTo(7);
    }

    @Test
    @DisplayName("Should have correct message constants")
    void shouldHaveCorrectMessageConstants() {
        // Test some key message constants
        assertThat(MessageConstants.TOKEN_TYPE_BEARER).isEqualTo("Bearer");
        assertThat(MessageConstants.AUTHORIZATION_HEADER).isEqualTo("Authorization");
        assertThat(MessageConstants.BEARER_PREFIX).isEqualTo("Bearer ");
        assertThat(MessageConstants.CHARSET_UTF8).isEqualTo("UTF-8");
    }

    @Test
    @DisplayName("Should have correct email constants")
    void shouldHaveCorrectEmailConstants() {
        // Test template placeholders
        assertThat(EmailConstants.PLACEHOLDER_USERNAME).isEqualTo("{{USERNAME}}");
        assertThat(EmailConstants.PLACEHOLDER_OTP).isEqualTo("{{OTP}}");
        assertThat(EmailConstants.PLACEHOLDER_DATE).isEqualTo("{{DATE}}");
        assertThat(EmailConstants.PLACEHOLDER_TIME).isEqualTo("{{TIME}}");
        
        // Test template names
        assertThat(EmailConstants.OTP_EMAIL_TEMPLATE).isEqualTo("otp-template.html");
        assertThat(EmailConstants.WELCOME_EMAIL_TEMPLATE).isEqualTo("welcome-template.html");
        assertThat(EmailConstants.PASSWORD_CHANGED_EMAIL_TEMPLATE).isEqualTo("password-changed-template.html");
        
        // Test SMTP properties
        assertThat(EmailConstants.SMTP_AUTH_PROPERTY).isEqualTo("mail.smtp.auth");
        assertThat(EmailConstants.SMTP_AUTH_VALUE).isEqualTo("true");
    }

    @Test
    @DisplayName("Should have email template constants that end with .html")
    void shouldHaveEmailTemplateConstantsThatEndWithHtml() {
        assertThat(EmailConstants.OTP_EMAIL_TEMPLATE).endsWith(".html");
        assertThat(EmailConstants.WELCOME_EMAIL_TEMPLATE).endsWith(".html");
        assertThat(EmailConstants.PASSWORD_CHANGED_EMAIL_TEMPLATE).endsWith(".html");
    }

    @Test
    @DisplayName("Should have placeholder constants in correct format")
    void shouldHavePlaceholderConstantsInCorrectFormat() {
        assertThat(EmailConstants.PLACEHOLDER_USERNAME).startsWith("{{").endsWith("}}");
        assertThat(EmailConstants.PLACEHOLDER_OTP).startsWith("{{").endsWith("}}");
        assertThat(EmailConstants.PLACEHOLDER_DATE).startsWith("{{").endsWith("}}");
        assertThat(EmailConstants.PLACEHOLDER_TIME).startsWith("{{").endsWith("}}");
    }

    @Test
    @DisplayName("Should have Bearer prefix with space")
    void shouldHaveBearerPrefixWithSpace() {
        assertThat(MessageConstants.BEARER_PREFIX).isEqualTo("Bearer ");
        assertThat(MessageConstants.BEARER_PREFIX).endsWith(" ");
        assertThat(MessageConstants.BEARER_PREFIX.length()).isEqualTo(7);
    }

    @Test
    @DisplayName("Should have correct validation constants")
    void shouldHaveCorrectValidationConstants() {
        // Password validation
        assertThat(ValidationConstants.PASSWORD_MIN_LENGTH).isEqualTo(8);
        assertThat(ValidationConstants.PASSWORD_MAX_LENGTH).isEqualTo(128);
        assertThat(ValidationConstants.PASSWORD_PATTERN).isEqualTo("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$");
        
        // Username validation
        assertThat(ValidationConstants.USERNAME_MIN_LENGTH).isEqualTo(3);
        assertThat(ValidationConstants.USERNAME_MAX_LENGTH).isEqualTo(50);
        assertThat(ValidationConstants.USERNAME_PATTERN).isEqualTo("^[a-zA-Z0-9._-]+$");
        
        // Email validation
        assertThat(ValidationConstants.EMAIL_MAX_LENGTH).isEqualTo(254);
        
        // OTP validation
        assertThat(ValidationConstants.OTP_PATTERN).isEqualTo("^[0-9]{6}$");
        
        // Validation messages
        assertThat(ValidationConstants.PASSWORD_BLANK_MESSAGE).isEqualTo("Password cannot be blank");
        assertThat(ValidationConstants.PASSWORD_SIZE_MESSAGE).isEqualTo("Password must be between 8 and 128 characters");
        assertThat(ValidationConstants.USERNAME_BLANK_MESSAGE).isEqualTo("Username cannot be blank");
        assertThat(ValidationConstants.EMAIL_BLANK_MESSAGE).isEqualTo("Email cannot be blank");
        assertThat(ValidationConstants.OTP_BLANK_MESSAGE).isEqualTo("OTP cannot be blank");
    }

    @Test
    @DisplayName("Should have consistent Bearer related constants")
    void shouldHaveConsistentBearerRelatedConstants() {
        // Token type should be Bearer without space
        assertThat(MessageConstants.TOKEN_TYPE_BEARER).isEqualTo("Bearer");
        
        // Bearer prefix should include space for Authorization header
        assertThat(MessageConstants.BEARER_PREFIX).isEqualTo("Bearer ");
        
        // Bearer token start index should match the length of "Bearer "
        assertThat(ApplicationConstants.BEARER_TOKEN_START_INDEX).isEqualTo(MessageConstants.BEARER_PREFIX.length());
    }
}
