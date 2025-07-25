package com.vn.caro_game.dtos;

import com.vn.caro_game.constants.HttpStatusConstants;
import com.vn.caro_game.dtos.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiResponse Tests")
class ApiResponseTest {

    @Test
    @DisplayName("Should create success response with data")
    void shouldCreateSuccessResponseWithData() {
        // Given
        Integer testData = 123; // Sử dụng Integer thay vì String

        // When
        ApiResponse<Integer> response = ApiResponse.success(testData);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusConstants.OK);
        assertThat(response.getData()).isEqualTo(testData);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    @DisplayName("Should create success response with message and data")
    void shouldCreateSuccessResponseWithMessageAndData() {
        // Given
        String message = "Operation successful";
        Integer testData = 456;

        // When
        ApiResponse<Integer> response = ApiResponse.success(message, testData);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusConstants.OK);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isEqualTo(testData);
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    @DisplayName("Should create success response with message only")
    void shouldCreateSuccessResponseWithMessageOnly() {
        // Given
        String message = "Operation completed";

        // When
        ApiResponse<Void> response = ApiResponse.success(message);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusConstants.OK);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    @DisplayName("Should create error response with message")
    void shouldCreateErrorResponseWithMessage() {
        // Given
        String errorMessage = "Something went wrong";

        // When
        ApiResponse<Object> response = ApiResponse.error(errorMessage);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusConstants.BAD_REQUEST);
        assertThat(response.getMessage()).isEqualTo(errorMessage);
        assertThat(response.getData()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    @DisplayName("Should create error response with message and error code")
    void shouldCreateErrorResponseWithMessageAndErrorCode() {
        // Given
        String errorMessage = "Validation failed";
        String errorCode = "VALIDATION_ERROR";

        // When
        ApiResponse<Object> response = ApiResponse.error(errorMessage, errorCode);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusConstants.BAD_REQUEST);
        assertThat(response.getMessage()).isEqualTo(errorMessage);
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("Should create error response with message, error code and status code")
    void shouldCreateErrorResponseWithMessageErrorCodeAndStatusCode() {
        // Given
        String errorMessage = "Not found";
        String errorCode = "NOT_FOUND";
        int statusCode = HttpStatusConstants.NOT_FOUND;

        // When
        ApiResponse<Object> response = ApiResponse.error(errorMessage, errorCode, statusCode);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getStatusCode()).isEqualTo(statusCode);
        assertThat(response.getMessage()).isEqualTo(errorMessage);
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("Should handle null data in success response")
    void shouldHandleNullDataInSuccessResponse() {
        // When
        ApiResponse<Void> response = ApiResponse.success("Test message");

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusConstants.OK);
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("Should build response using builder pattern")
    void shouldBuildResponseUsingBuilderPattern() {
        // Given
        String message = "Custom response";
        Integer data = 789;
        int statusCode = HttpStatusConstants.OK;

        // When
        ApiResponse<Integer> response = ApiResponse.<Integer>builder()
                .success(true)
                .statusCode(statusCode)
                .message(message)
                .data(data)
                .build();

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(statusCode);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isEqualTo(data);
    }

    @Test
    @DisplayName("Should handle complex object as data")
    void shouldHandleComplexObjectAsData() {
        // Given
        TestData testData = new TestData("test", 123);

        // When
        ApiResponse<TestData> response = ApiResponse.success("Success with object", testData);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(testData);
        assertThat(response.getData().getName()).isEqualTo("test");
        assertThat(response.getData().getValue()).isEqualTo(123);
    }

    // Helper class for testing
    private static class TestData {
        private final String name;
        private final int value;

        public TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestData testData = (TestData) obj;
            return value == testData.value && name.equals(testData.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode() + Integer.hashCode(value);
        }
    }
}
