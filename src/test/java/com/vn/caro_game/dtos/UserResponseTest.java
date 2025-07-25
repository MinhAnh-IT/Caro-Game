package com.vn.caro_game.dtos;

import com.vn.caro_game.dtos.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserResponse DTO Tests")
class UserResponseTest {

    @Test
    @DisplayName("Should create UserResponse with builder")
    void shouldCreateUserResponseWithBuilder() {
        // Given
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String avatarUrl = "https://example.com/avatar.jpg";
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        UserResponse response = UserResponse.builder()
                .id(id)
                .username(username)
                .email(email)
                .avatarUrl(avatarUrl)
                .createdAt(createdAt)
                .build();

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getAvatarUrl()).isEqualTo(avatarUrl);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Should create UserResponse with NoArgsConstructor")
    void shouldCreateUserResponseWithNoArgsConstructor() {
        // When
        UserResponse response = new UserResponse();

        // Then
        assertThat(response.getId()).isNull();
        assertThat(response.getUsername()).isNull();
        assertThat(response.getEmail()).isNull();
        assertThat(response.getAvatarUrl()).isNull();
        assertThat(response.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Should create UserResponse with AllArgsConstructor")
    void shouldCreateUserResponseWithAllArgsConstructor() {
        // Given
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String avatarUrl = "https://example.com/avatar.jpg";
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        UserResponse response = new UserResponse(id, username, email, avatarUrl, createdAt);

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getAvatarUrl()).isEqualTo(avatarUrl);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void shouldHandleNullValuesCorrectly() {
        // When
        UserResponse response = UserResponse.builder()
                .id(null)
                .username(null)
                .email(null)
                .avatarUrl(null)
                .createdAt(null)
                .build();

        // Then
        assertThat(response.getId()).isNull();
        assertThat(response.getUsername()).isNull();
        assertThat(response.getEmail()).isNull();
        assertThat(response.getAvatarUrl()).isNull();
        assertThat(response.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Should support setter and getter methods")
    void shouldSupportSetterAndGetterMethods() {
        // Given
        UserResponse response = new UserResponse();
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String avatarUrl = "https://example.com/avatar.jpg";
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        response.setId(id);
        response.setUsername(username);
        response.setEmail(email);
        response.setAvatarUrl(avatarUrl);
        response.setCreatedAt(createdAt);

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getAvatarUrl()).isEqualTo(avatarUrl);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Should create UserResponse with builder pattern")
    void shouldCreateUserResponseWithBuilderPattern() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .avatarUrl("https://example.com/avatar.jpg")
                .createdAt(now)
                .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("john_doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }
}
