package com.vn.caro_game.exceptions;

import com.vn.caro_game.constants.HttpStatusConstants;
import com.vn.caro_game.constants.MessageConstants;
import com.vn.caro_game.dtos.response.ApiResponse;
import com.vn.caro_game.enums.StatusCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle CustomException with EMAIL_ALREADY_EXISTS")
    void shouldHandleCustomExceptionWithEmailAlreadyExists() {
        // Given
        CustomException exception = new CustomException(StatusCode.EMAIL_ALREADY_EXISTS);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleCustomException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatusConstants.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo(StatusCode.EMAIL_ALREADY_EXISTS.getMessage());
        assertThat(response.getBody().getErrorCode()).isEqualTo("EMAIL_ALREADY_EXISTS");
    }

    @Test
    @DisplayName("Should handle CustomException with UNAUTHORIZED")
    void shouldHandleCustomExceptionWithUnauthorized() {
        // Given
        CustomException exception = new CustomException(StatusCode.UNAUTHORIZED);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleCustomException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatusConstants.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo(StatusCode.UNAUTHORIZED.getMessage());
    }

    @Test
    @DisplayName("Should handle CustomException with NOT_FOUND")
    void shouldHandleCustomExceptionWithNotFound() {
        // Given
        CustomException exception = new CustomException(StatusCode.USER_NOT_FOUND);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleCustomException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatusConstants.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo(StatusCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Should handle CustomException with TOO_MANY_REQUESTS")
    void shouldHandleCustomExceptionWithTooManyRequests() {
        // Given
        CustomException exception = new CustomException(StatusCode.OTP_ATTEMPTS_EXCEEDED);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleCustomException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatusConstants.TOO_MANY_REQUESTS);
        assertThat(response.getBody().getMessage()).isEqualTo(StatusCode.OTP_ATTEMPTS_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void shouldHandleMethodArgumentNotValidException() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("userCreation", "email", "Email is required");
        FieldError fieldError2 = new FieldError("userCreation", "password", "Password must be at least 6 characters");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2);
        
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleValidationException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatusConstants.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo(MessageConstants.INVALID_INPUT_DATA);
        assertThat(response.getBody().getErrorCode()).isEqualTo(MessageConstants.VALIDATION_ERROR_CODE);
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void shouldHandleGenericException() {
        // Given
        Exception exception = new RuntimeException("Unexpected error occurred");

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatusConstants.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo(MessageConstants.UNEXPECTED_ERROR);
    }

    @Test
    @DisplayName("Should map StatusCode BAD_REQUEST to correct HTTP status")
    void shouldMapStatusCodeBadRequestToCorrectHttpStatus() {
        // Given
        CustomException exception = new CustomException(StatusCode.INVALID_OTP);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleCustomException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatusConstants.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should map StatusCode FORBIDDEN to correct HTTP status")
    void shouldMapStatusCodeForbiddenToCorrectHttpStatus() {
        // Given
        CustomException exception = new CustomException(StatusCode.FORBIDDEN);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleCustomException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatusConstants.FORBIDDEN);
    }

    @Test
    @DisplayName("Should handle CustomException with unknown status code")
    void shouldHandleCustomExceptionWithUnknownStatusCode() {
        // Given - Using a status code that should default to INTERNAL_SERVER_ERROR
        CustomException exception = new CustomException(StatusCode.EMAIL_SEND_FAILED);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleCustomException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatusConstants.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Should preserve error message and code from CustomException")
    void shouldPreserveErrorMessageAndCodeFromCustomException() {
        // Given
        CustomException exception = new CustomException(StatusCode.USERNAME_ALREADY_EXISTS);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleCustomException(exception);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(StatusCode.USERNAME_ALREADY_EXISTS.getMessage());
        assertThat(response.getBody().getErrorCode()).isEqualTo("USERNAME_ALREADY_EXISTS");
    }

    @Test
    @DisplayName("Should handle validation errors with empty field errors list")
    void shouldHandleValidationErrorsWithEmptyFieldErrorsList() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList());
        
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleValidationException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo(MessageConstants.INVALID_INPUT_DATA);
        assertThat(response.getBody().getData()).isNotNull();
    }
}
