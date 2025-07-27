package com.vn.caro_game.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Entity Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    @DisplayName("Should create User with default values")
    void shouldCreateUserWithDefaultValues() {
        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getAvatarUrl()).isNull();
        assertThat(user.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Should set and get all User fields correctly")
    void shouldSetAndGetAllUserFieldsCorrectly() {
        // Given
        Long id = 1L;
        String username = "testuser";
        String password = "password123";
        String email = "test@example.com";
        String avatarUrl = "http://example.com/avatar.jpg";
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setAvatarUrl(avatarUrl);
        user.setCreatedAt(createdAt);

        // Then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getAvatarUrl()).isEqualTo(avatarUrl);
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void shouldHandleNullValuesCorrectly() {
        // When - Set fields to null
        user.setId(null);
        user.setUsername(null);
        user.setPassword(null);
        user.setEmail(null);
        user.setAvatarUrl(null);
        user.setCreatedAt(null);

        // Then
        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getAvatarUrl()).isNull();
        assertThat(user.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Should update User fields correctly")
    void shouldUpdateUserFieldsCorrectly() {
        // Given - Initial values
        user.setUsername("oldusername");
        user.setEmail("old@example.com");

        // When - Update values
        user.setUsername("newusername");
        user.setEmail("new@example.com");

        // Then
        assertThat(user.getUsername()).isEqualTo("newusername");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("Should handle LocalDateTime for createdAt correctly")
    void shouldHandleLocalDateTimeForCreatedAtCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        LocalDateTime future = LocalDateTime.now().plusDays(1);

        // When & Then - Test current time
        user.setCreatedAt(now);
        assertThat(user.getCreatedAt()).isEqualTo(now);

        // When & Then - Test past time
        user.setCreatedAt(past);
        assertThat(user.getCreatedAt()).isEqualTo(past);
        assertThat(user.getCreatedAt()).isBefore(now);

        // When & Then - Test future time
        user.setCreatedAt(future);
        assertThat(user.getCreatedAt()).isEqualTo(future);
        assertThat(user.getCreatedAt()).isAfter(now);
    }

    @Test
    @DisplayName("Should handle empty strings correctly")
    void shouldHandleEmptyStringsCorrectly() {
        // When
        user.setUsername("");
        user.setPassword("");
        user.setEmail("");
        user.setAvatarUrl("");

        // Then
        assertThat(user.getUsername()).isEmpty();
        assertThat(user.getPassword()).isEmpty();
        assertThat(user.getEmail()).isEmpty();
        assertThat(user.getAvatarUrl()).isEmpty();
    }

    @Test
    @DisplayName("Should handle long strings correctly")
    void shouldHandleLongStringsCorrectly() {
        // Given
        String longString = "a".repeat(1000);

        // When
        user.setUsername(longString);
        user.setPassword(longString);
        user.setEmail(longString);
        user.setAvatarUrl(longString);

        // Then
        assertThat(user.getUsername()).hasSize(1000);
        assertThat(user.getPassword()).hasSize(1000);
        assertThat(user.getEmail()).hasSize(1000);
        assertThat(user.getAvatarUrl()).hasSize(1000);
    }

    @Test
    @DisplayName("Should create different User instances")
    void shouldCreateDifferentUserInstances() {
        // Given
        User user1 = new User();
        User user2 = new User();

        // When
        user1.setUsername("user1");
        user2.setUsername("user2");

        // Then
        assertThat(user1).isNotSameAs(user2);
        assertThat(user1.getUsername()).isNotEqualTo(user2.getUsername());
    }
}
