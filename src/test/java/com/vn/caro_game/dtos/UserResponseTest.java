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
        String displayName = "Test User";
        String avatarUrl = "https://example.com/avatar.jpg";
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        UserResponse response = UserResponse.builder()
                .id(id)
                .username(username)
                .email(email)
                .displayName(displayName)
                .avatarUrl(avatarUrl)
                .createdAt(createdAt)
                .build();

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getDisplayName()).isEqualTo(displayName);
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
        assertThat(response.getDisplayName()).isNull();
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
        String displayName = "Test User";
        String avatarUrl = "https://example.com/avatar.jpg";
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        UserResponse response = new UserResponse(id, username, email, displayName, avatarUrl, createdAt);

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getDisplayName()).isEqualTo(displayName);
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
                .displayName(null)
                .avatarUrl(null)
                .createdAt(null)
                .build();

        // Then
        assertThat(response.getId()).isNull();
        assertThat(response.getUsername()).isNull();
        assertThat(response.getEmail()).isNull();
        assertThat(response.getDisplayName()).isNull();
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
        String displayName = "Test User";
        String avatarUrl = "https://example.com/avatar.jpg";
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        response.setId(id);
        response.setUsername(username);
        response.setEmail(email);
        response.setDisplayName(displayName);
        response.setAvatarUrl(avatarUrl);
        response.setCreatedAt(createdAt);

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getDisplayName()).isEqualTo(displayName);
        assertThat(response.getAvatarUrl()).isEqualTo(avatarUrl);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }
}
