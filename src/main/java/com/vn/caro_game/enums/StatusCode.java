package com.vn.caro_game.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum StatusCode {
    SUCCESS(200, "Success"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    
    // Authentication errors
    EMAIL_ALREADY_EXISTS(4001, "Email already exists"),
    USERNAME_ALREADY_EXISTS(4002, "Username already exists"),
    INVALID_CREDENTIALS(4003, "Invalid email or password"),
    ACCOUNT_LOCKED(4004, "Account is temporarily locked"),
    CURRENT_PASSWORD_INCORRECT(4005, "Current password is incorrect"),
    INVALID_OTP(4006, "Invalid or expired OTP"),
    OTP_ATTEMPTS_EXCEEDED(4007, "OTP attempts exceeded. Please try again later"),
    INVALID_REFRESH_TOKEN(4008, "Invalid or expired refresh token"),
    USER_NOT_FOUND(4009, "User not found"),
    EMAIL_NOT_FOUND(4010, "Email not found in system"),
    
    // File handling errors
    ERROR_CONVERT_IMAGE(5001, "Error converting image"),
    ERROR_SAVE_FILE(5002, "Error while saving file"),
    
    // Email errors
    EMAIL_SEND_FAILED(5003, "Failed to send email");
    
    int code;
    String message;

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
