package com.vn.caro_game.exceptions;

import com.vn.caro_game.enums.StatusCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomException extends RuntimeException {
    StatusCode statusCode;
    
    public CustomException(StatusCode statusCode) {
        super(statusCode.getMessage());
        this.statusCode = statusCode;
    }
    
    public CustomException(StatusCode statusCode, Throwable cause) {
        super(statusCode.getMessage(), cause);
        this.statusCode = statusCode;
    }
    
    // Keep this constructor for backward compatibility with custom messages
    public CustomException(String message, StatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
