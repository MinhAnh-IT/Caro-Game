package com.vn.caro_game.dtos;

import com.vn.caro_game.dtos.response.UserResponse;
import com.vn.caro_game.enums.UserStatus;
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
        UserStatus status = UserStatus.ONLINE;
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        UserResponse response = UserResponse.builder()
                .id(id)
                .username(username)
                .email(email)
                .avatarUrl(avatarUrl)
                .status(status)
                .createdAt(createdAt)
                .build();

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getAvatarUrl()).isEqualTo(avatarUrl);
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Should create UserResponse with NoArgsConstructor")
    void shouldCreateUserResponseWithNoArgsConstructor() {
        // When
        UserResponse response = new UserResponse();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNull();
        assertThat(response.getUsername()).isNull();
        assertThat(response.getEmail()).isNull();
        assertThat(response.getAvatarUrl()).isNull();
        assertThat(response.getStatus()).isNull();
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
        UserStatus status = UserStatus.ONLINE;
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        UserResponse response = new UserResponse(id, username, email, avatarUrl, status, createdAt);

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getAvatarUrl()).isEqualTo(avatarUrl);
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Should set and get all fields")
    void shouldSetAndGetAllFields() {
        // Given
        UserResponse response = new UserResponse();
        Long id = 123L;
        String username = "newuser";
        String email = "new@example.com";
        String avatarUrl = "https://example.com/new-avatar.jpg";
        UserStatus status = UserStatus.OFFLINE;
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 12, 0);

        // When
        response.setId(id);
        response.setUsername(username);
        response.setEmail(email);
        response.setAvatarUrl(avatarUrl);
        response.setStatus(status);
        response.setCreatedAt(createdAt);

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getAvatarUrl()).isEqualTo(avatarUrl);
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Should handle null values")
    void shouldHandleNullValues() {
        // When
        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("user")
                .email(null)
                .avatarUrl(null)
                .status(null)
                .createdAt(null)
                .build();

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("user");
        assertThat(response.getEmail()).isNull();
        assertThat(response.getAvatarUrl()).isNull();
        assertThat(response.getStatus()).isNull();
        assertThat(response.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Should support all UserStatus values")
    void shouldSupportAllUserStatuses() {
        // Given & When & Then
        for (UserStatus status : UserStatus.values()) {
            UserResponse response = UserResponse.builder()
                    .status(status)
                    .build();
            
            assertThat(response.getStatus()).isEqualTo(status);
        }
    }

    @Test
    @DisplayName("Should create response with basic info")
    void shouldCreateBasicUserResponse() {
        // Given
        String username = "john_doe";
        String email = "john@example.com";

        // When
        UserResponse response = UserResponse.builder()
                .username(username)
                .email(email)
                .status(UserStatus.ONLINE)
                .build();

        // Then
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getStatus()).isEqualTo(UserStatus.ONLINE);
        assertThat(response.getId()).isNull();
        assertThat(response.getAvatarUrl()).isNull();
        assertThat(response.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Should create response with complete info")
    void shouldCreateCompleteUserResponse() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        UserResponse response = UserResponse.builder()
                .id(999L)
                .username("admin")
                .email("admin@example.com")
                .avatarUrl("https://example.com/admin-avatar.png")
                .status(UserStatus.ONLINE)
                .createdAt(now)
                .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(999L);
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getEmail()).isEqualTo("admin@example.com");
        assertThat(response.getAvatarUrl()).isEqualTo("https://example.com/admin-avatar.png");
        assertThat(response.getStatus()).isEqualTo(UserStatus.ONLINE);
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }
}
