package com.vn.caro_game.exceptions;

import com.vn.caro_game.enums.StatusCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CustomException Tests")
class CustomExceptionTest {

    @Test
    @DisplayName("Nên tạo CustomException với StatusCode")
    void shouldCreateCustomExceptionWithStatusCode() {
        // Given
        StatusCode statusCode = StatusCode.USER_NOT_FOUND;

        // When
        CustomException exception = new CustomException(statusCode);

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(statusCode);
        assertThat(exception.getMessage()).isEqualTo(statusCode.getMessage());
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Nên tạo CustomException với StatusCode và cause")
    void shouldCreateCustomExceptionWithStatusCodeAndCause() {
        // Given
        StatusCode statusCode = StatusCode.INTERNAL_SERVER_ERROR;
        Throwable cause = new RuntimeException("Original cause");

        // When
        CustomException exception = new CustomException(statusCode, cause);

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(statusCode);
        assertThat(exception.getMessage()).isEqualTo(statusCode.getMessage());
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Nên tạo CustomException với custom message và StatusCode")
    void shouldCreateCustomExceptionWithCustomMessageAndStatusCode() {
        // Given
        String customMessage = "Custom error message";
        StatusCode statusCode = StatusCode.BAD_REQUEST;

        // When
        CustomException exception = new CustomException(customMessage, statusCode);

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(statusCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Nên là RuntimeException")
    void shouldBeRuntimeException() {
        // Given
        CustomException exception = new CustomException(StatusCode.USER_NOT_FOUND);

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Nên test với tất cả các StatusCode")
    void shouldTestWithAllStatusCodes() {
        for (StatusCode statusCode : StatusCode.values()) {
            // When
            CustomException exception = new CustomException(statusCode);

            // Then
            assertThat(exception.getStatusCode()).isEqualTo(statusCode);
            assertThat(exception.getMessage()).isEqualTo(statusCode.getMessage());
        }
    }

    @Test
    @DisplayName("Nên giữ được cause trong exception chain")
    void shouldPreserveCauseInExceptionChain() {
        // Given
        Exception originalException = new IllegalArgumentException("Original error");
        StatusCode statusCode = StatusCode.BAD_REQUEST;

        // When
        CustomException exception = new CustomException(statusCode, originalException);

        // Then
        assertThat(exception.getCause()).isEqualTo(originalException);
        assertThat(exception.getCause().getMessage()).isEqualTo("Original error");
    }

    @Test
    @DisplayName("Nên có thể nest multiple exceptions")
    void shouldAllowNestingMultipleExceptions() {
        // Given
        Exception level1 = new RuntimeException("Level 1");
        Exception level2 = new IllegalStateException("Level 2", level1);
        StatusCode statusCode = StatusCode.INTERNAL_SERVER_ERROR;

        // When
        CustomException exception = new CustomException(statusCode, level2);

        // Then
        assertThat(exception.getCause()).isEqualTo(level2);
        assertThat(exception.getCause().getCause()).isEqualTo(level1);
    }

    @Test
    @DisplayName("Nên có custom message override StatusCode message")
    void shouldCustomMessageOverrideStatusCodeMessage() {
        // Given
        StatusCode statusCode = StatusCode.USER_NOT_FOUND;
        String customMessage = "User with specific ID not found";

        // When
        CustomException exception = new CustomException(customMessage, statusCode);

        // Then
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getMessage()).isNotEqualTo(statusCode.getMessage());
        assertThat(exception.getStatusCode()).isEqualTo(statusCode);
    }

    @Test
    @DisplayName("Nên xử lý null values đúng cách")
    void shouldHandleNullValuesCorrectly() {
        // Given
        StatusCode statusCode = StatusCode.BAD_REQUEST;

        // When
        CustomException exceptionWithNullCause = new CustomException(statusCode, null);
        CustomException exceptionWithNullMessage = new CustomException(null, statusCode);

        // Then
        assertThat(exceptionWithNullCause.getCause()).isNull();
        assertThat(exceptionWithNullCause.getStatusCode()).isEqualTo(statusCode);
        
        assertThat(exceptionWithNullMessage.getMessage()).isNull();
        assertThat(exceptionWithNullMessage.getStatusCode()).isEqualTo(statusCode);
    }
}
