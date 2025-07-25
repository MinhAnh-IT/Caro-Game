package com.vn.caro_game.controllers.base;

import com.vn.caro_game.dtos.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Base controller providing common response methods
 * Follows clean architecture principles with consistent API responses
 */
public abstract class BaseController {
    
    /**
     * Create successful response with data
     */
    protected <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(
            ApiResponse.<T>builder()
                .statusCode(HttpStatus.OK.value())
                .message(message)
                .success(true)
                .data(data)
                .build()
        );
    }
    
    /**
     * Create successful response with default message
     */
    protected <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return success(data, "Operation completed successfully");
    }
    
    /**
     * Create successful response without data
     */
    protected ResponseEntity<ApiResponse<Void>> success(String message) {
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message(message)
                .success(true)
                .build()
        );
    }
    
    /**
     * Create created response (201)
     */
    protected <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.<T>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message(message)
                .success(true)
                .data(data)
                .build()
        );
    }
    
    /**
     * Create accepted response (202) for async operations
     */
    protected ResponseEntity<ApiResponse<Void>> accepted(String message) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
            ApiResponse.<Void>builder()
                .statusCode(HttpStatus.ACCEPTED.value())
                .message(message)
                .success(true)
                .build()
        );
    }
}
