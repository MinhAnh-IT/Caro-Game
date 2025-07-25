package com.vn.caro_game.mappers;

import com.vn.caro_game.dtos.request.UserCreation;
import com.vn.caro_game.dtos.response.UserResponse;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("UserMapper Tests")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("Should map UserCreation to User entity")
    void shouldMapUserCreationToUserEntity() {
        // Given
        UserCreation userCreation = new UserCreation();
        userCreation.setUsername("testuser");
        userCreation.setEmail("test@example.com");
        userCreation.setPassword("password123");

        // When
        User user = userMapper.toEntity(userCreation);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("password123");
        
        // These should be ignored and null/default
        assertThat(user.getId()).isNull();
        assertThat(user.getAvatarUrl()).isNull();
        assertThat(user.getStatus()).isEqualTo(UserStatus.OFFLINE); // Default status
        assertThat(user.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Should map User entity to UserResponse")
    void shouldMapUserEntityToUserResponse() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123"); // This should not be included in response
        user.setAvatarUrl("http://example.com/avatar.jpg");
        user.setStatus(UserStatus.ONLINE);
        user.setCreatedAt(now);

        // When
        UserResponse userResponse = userMapper.toResponse(user);

        // Then
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getId()).isEqualTo(1L);
        assertThat(userResponse.getUsername()).isEqualTo("testuser");
        assertThat(userResponse.getEmail()).isEqualTo("test@example.com");
        assertThat(userResponse.getAvatarUrl()).isEqualTo("http://example.com/avatar.jpg");
        assertThat(userResponse.getStatus()).isEqualTo(UserStatus.ONLINE);
        assertThat(userResponse.getCreatedAt()).isEqualTo(now);
        
        // Password should not be included in response (UserResponse doesn't have password field)
    }

    @Test
    @DisplayName("Should handle null UserCreation")
    void shouldHandleNullUserCreation() {
        // When
        User user = userMapper.toEntity(null);

        // Then
        assertThat(user).isNull();
    }

    @Test
    @DisplayName("Should handle null User entity")
    void shouldHandleNullUserEntity() {
        // When
        UserResponse userResponse = userMapper.toResponse(null);

        // Then
        assertThat(userResponse).isNull();
    }

    @Test
    @DisplayName("Should map User entity with null fields to UserResponse")
    void shouldMapUserEntityWithNullFieldsToUserResponse() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        // Leave other fields as null

        // When
        UserResponse userResponse = userMapper.toResponse(user);

        // Then
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getId()).isEqualTo(1L);
        assertThat(userResponse.getUsername()).isEqualTo("testuser");
        assertThat(userResponse.getEmail()).isEqualTo("test@example.com");
        assertThat(userResponse.getAvatarUrl()).isNull();
        assertThat(userResponse.getStatus()).isEqualTo(UserStatus.OFFLINE); // Default status
        assertThat(userResponse.getCreatedAt()).isNull();
    }
}
