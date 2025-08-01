package com.vn.caro_game.exceptions;

import com.vn.caro_game.constants.HttpStatusConstants;
import com.vn.caro_game.constants.MessageConstants;
import com.vn.caro_game.dtos.response.ApiResponse;
import com.vn.caro_game.enums.StatusCode;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException ex) {
        log.error("Custom exception occurred: {}", ex.getMessage(), ex);
        
        HttpStatus httpStatus = mapStatusCodeToHttpStatus(ex.getStatusCode());
        int statusCode = mapStatusCodeToInt(ex.getStatusCode());
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), ex.getStatusCode().name(), statusCode);
        
        return ResponseEntity.status(httpStatus).body(response);
    }
    
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileUploadException(FileUploadException ex) {
        log.error("File upload exception occurred: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.error(
            ex.getMessage(),
            "FILE_UPLOAD_ERROR",
            HttpStatusConstants.BAD_REQUEST
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation exception occurred", ex);
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .statusCode(HttpStatusConstants.BAD_REQUEST)
                .message(MessageConstants.INVALID_INPUT_DATA)
                .data(errors)
                .errorCode(MessageConstants.VALIDATION_ERROR_CODE)
                .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected exception occurred", ex);
        
        ApiResponse<Object> response = ApiResponse.error(
            MessageConstants.UNEXPECTED_ERROR, 
            StatusCode.INTERNAL_SERVER_ERROR.name(),
            HttpStatusConstants.INTERNAL_SERVER_ERROR
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    private HttpStatus mapStatusCodeToHttpStatus(StatusCode statusCode) {
        return switch (statusCode) {
            case BAD_REQUEST, EMAIL_ALREADY_EXISTS, USERNAME_ALREADY_EXISTS,
                 CURRENT_PASSWORD_INCORRECT, INVALID_OTP, INVALID_REQUEST,
                 FILE_TOO_LARGE, INVALID_FILE_TYPE, FILE_UPLOAD_ERROR -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED, INVALID_CREDENTIALS, INVALID_REFRESH_TOKEN -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN, ACCOUNT_LOCKED -> HttpStatus.FORBIDDEN;
            case NOT_FOUND, USER_NOT_FOUND, EMAIL_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case TOO_MANY_REQUESTS, OTP_ATTEMPTS_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
    
    private int mapStatusCodeToInt(StatusCode statusCode) {
        return switch (statusCode) {
            case BAD_REQUEST, EMAIL_ALREADY_EXISTS, USERNAME_ALREADY_EXISTS,
                 CURRENT_PASSWORD_INCORRECT, INVALID_OTP, INVALID_REQUEST,
                 FILE_TOO_LARGE, INVALID_FILE_TYPE, FILE_UPLOAD_ERROR -> HttpStatusConstants.BAD_REQUEST;
            case UNAUTHORIZED, INVALID_CREDENTIALS, INVALID_REFRESH_TOKEN -> HttpStatusConstants.UNAUTHORIZED;
            case FORBIDDEN, ACCOUNT_LOCKED -> HttpStatusConstants.FORBIDDEN;
            case NOT_FOUND, USER_NOT_FOUND, EMAIL_NOT_FOUND -> HttpStatusConstants.NOT_FOUND;
            case TOO_MANY_REQUESTS, OTP_ATTEMPTS_EXCEEDED -> HttpStatusConstants.TOO_MANY_REQUESTS;
            default -> HttpStatusConstants.INTERNAL_SERVER_ERROR;
        };
    }
}
