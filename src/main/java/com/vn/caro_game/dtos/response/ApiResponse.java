package com.vn.caro_game.dtos.response;

import com.vn.caro_game.constants.HttpStatusConstants;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    boolean success;
    int statusCode;
    String message;
    T data;
    String errorCode;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(HttpStatusConstants.OK)
                .data(data)
                .build();
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(HttpStatusConstants.OK)
                .message(message)
                .data(data)
                .build();
    }
    
    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .statusCode(HttpStatusConstants.OK)
                .message(message)
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .statusCode(HttpStatusConstants.BAD_REQUEST)
                .message(message)
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .statusCode(HttpStatusConstants.BAD_REQUEST)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, String errorCode, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .statusCode(statusCode)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
