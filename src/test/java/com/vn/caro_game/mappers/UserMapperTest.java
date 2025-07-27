package com.vn.caro_game.mappers;

import com.vn.caro_game.dtos.request.UpdateProfileRequest;
import com.vn.caro_game.dtos.request.UserCreation;
import com.vn.caro_game.dtos.response.UserProfileResponse;
import com.vn.caro_game.dtos.response.UserResponse;
import com.vn.caro_game.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for UserMapper.
 *
 * <p>This test class covers all mapping functionality between User entity
 * and various DTOs including registration, profile management, and responses.</p>
 */
@DisplayName("UserMapper Tests")
class UserMapperTest {

    private UserMapper userMapper;
    private User testUser;
    private UserCreation userCreation;
    private UpdateProfileRequest updateRequest;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setDisplayName("Test User");
        testUser.setAvatarUrl("/uploads/avatars/test.jpg");
        testUser.setCreatedAt(LocalDateTime.now());

        userCreation = new UserCreation();
        userCreation.setUsername("newuser");
        userCreation.setDisplayName("New User");
        userCreation.setEmail("newuser@example.com");
        userCreation.setPassword("password123");

        updateRequest = new UpdateProfileRequest();
        updateRequest.setUsername("updateduser");
        updateRequest.setDisplayName("Updated User");
    }

    @Nested
    @DisplayName("Entity Creation Tests")
    class EntityCreationTests {

        @Test
        @DisplayName("Should convert UserCreation to User entity successfully")
        void shouldConvertUserCreationToUserEntitySuccessfully() {
            // When
            User result = userMapper.toEntity(userCreation);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("newuser");
            assertThat(result.getDisplayName()).isEqualTo("New User");
            assertThat(result.getEmail()).isEqualTo("newuser@example.com");
            assertThat(result.getPassword()).isNull(); // Password should not be set by mapper
            assertThat(result.getId()).isNull(); // ID should not be set
            assertThat(result.getAvatarUrl()).isNull();
            assertThat(result.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null UserCreation gracefully")
        void shouldHandleNullUserCreationGracefully() {
            // When
            User result = userMapper.toEntity(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle UserCreation with null displayName")
        void shouldHandleUserCreationWithNullDisplayName() {
            // Given
            userCreation.setDisplayName(null);

            // When
            User result = userMapper.toEntity(userCreation);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("newuser");
            assertThat(result.getDisplayName()).isNull();
            assertThat(result.getEmail()).isEqualTo("newuser@example.com");
        }
    }

    @Nested
    @DisplayName("UserResponse Mapping Tests")
    class UserResponseMappingTests {

        @Test
        @DisplayName("Should convert User entity to UserResponse successfully")
        void shouldConvertUserEntityToUserResponseSuccessfully() {
            // When
            UserResponse result = userMapper.toResponse(testUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getDisplayName()).isEqualTo("Test User");
            assertThat(result.getAvatarUrl()).isEqualTo("/uploads/avatars/test.jpg");
            assertThat(result.getCreatedAt()).isEqualTo(testUser.getCreatedAt());
        }

        @Test
        @DisplayName("Should handle null User entity gracefully")
        void shouldHandleNullUserEntityGracefully() {
            // When
            UserResponse result = userMapper.toResponse(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle User entity with null fields")
        void shouldHandleUserEntityWithNullFields() {
            // Given
            testUser.setDisplayName(null);
            testUser.setAvatarUrl(null);

            // When
            UserResponse result = userMapper.toResponse(testUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getDisplayName()).isNull();
            assertThat(result.getAvatarUrl()).isNull();
            assertThat(result.getCreatedAt()).isEqualTo(testUser.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("UserProfileResponse Mapping Tests")
    class UserProfileResponseMappingTests {

        @Test
        @DisplayName("Should convert User entity to UserProfileResponse successfully")
        void shouldConvertUserEntityToUserProfileResponseSuccessfully() {
            // When
            UserProfileResponse result = userMapper.toProfileResponse(testUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getDisplayName()).isEqualTo("Test User");
            assertThat(result.getAvatarUrl()).isEqualTo("/uploads/avatars/test.jpg");
            assertThat(result.getCreatedAt()).isEqualTo(testUser.getCreatedAt());
        }

        @Test
        @DisplayName("Should handle null User entity for profile response gracefully")
        void shouldHandleNullUserEntityForProfileResponseGracefully() {
            // When
            UserProfileResponse result = userMapper.toProfileResponse(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle User entity with minimal data")
        void shouldHandleUserEntityWithMinimalData() {
            // Given
            User minimalUser = new User();
            minimalUser.setId(2L);
            minimalUser.setUsername("minimal");
            minimalUser.setEmail("minimal@example.com");
            // No displayName, avatarUrl, createdAt

            // When
            UserProfileResponse result = userMapper.toProfileResponse(minimalUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getUsername()).isEqualTo("minimal");
            assertThat(result.getEmail()).isEqualTo("minimal@example.com");
            assertThat(result.getDisplayName()).isNull();
            assertThat(result.getAvatarUrl()).isNull();
            assertThat(result.getCreatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("Entity Update Tests")
    class EntityUpdateTests {

        @Test
        @DisplayName("Should update User entity from UpdateProfileRequest successfully")
        void shouldUpdateUserEntityFromUpdateProfileRequestSuccessfully() {
            // Given
            String originalEmail = testUser.getEmail();
            Long originalId = testUser.getId();
            LocalDateTime originalCreatedAt = testUser.getCreatedAt();
            String originalAvatarUrl = testUser.getAvatarUrl();

            // When
            userMapper.updateUserFromRequest(testUser, updateRequest);

            // Then
            assertThat(testUser.getUsername()).isEqualTo("updateduser");
            assertThat(testUser.getDisplayName()).isEqualTo("Updated User");

            // These fields should remain unchanged
            assertThat(testUser.getEmail()).isEqualTo(originalEmail);
            assertThat(testUser.getId()).isEqualTo(originalId);
            assertThat(testUser.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(testUser.getAvatarUrl()).isEqualTo(originalAvatarUrl);
        }

        @Test
        @DisplayName("Should handle null User entity gracefully during update")
        void shouldHandleNullUserEntityGracefullyDuringUpdate() {
            // When & Then - Should not throw exception
            assertThatCode(() -> userMapper.updateUserFromRequest(null, updateRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle null UpdateProfileRequest gracefully")
        void shouldHandleNullUpdateProfileRequestGracefully() {
            // Given
            String originalUsername = testUser.getUsername();
            String originalDisplayName = testUser.getDisplayName();

            // When
            userMapper.updateUserFromRequest(testUser, null);

            // Then - User should remain unchanged
            assertThat(testUser.getUsername()).isEqualTo(originalUsername);
            assertThat(testUser.getDisplayName()).isEqualTo(originalDisplayName);
        }

        @Test
        @DisplayName("Should handle UpdateProfileRequest with null displayName")
        void shouldHandleUpdateProfileRequestWithNullDisplayName() {
            // Given
            updateRequest.setDisplayName(null);

            // When
            userMapper.updateUserFromRequest(testUser, updateRequest);

            // Then
            assertThat(testUser.getUsername()).isEqualTo("updateduser");
            assertThat(testUser.getDisplayName()).isNull();
        }

        @Test
        @DisplayName("Should handle both null parameters gracefully")
        void shouldHandleBothNullParametersGracefully() {
            // When & Then - Should not throw exception
            assertThatCode(() -> userMapper.updateUserFromRequest(null, null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle User entity with empty strings")
        void shouldHandleUserEntityWithEmptyStrings() {
            // Given
            testUser.setUsername("");
            testUser.setEmail("");
            testUser.setDisplayName("");
            testUser.setAvatarUrl("");

            // When
            UserResponse response = userMapper.toResponse(testUser);
            UserProfileResponse profileResponse = userMapper.toProfileResponse(testUser);

            // Then
            assertThat(response.getUsername()).isEmpty();
            assertThat(response.getEmail()).isEmpty();
            assertThat(response.getDisplayName()).isEmpty();
            assertThat(response.getAvatarUrl()).isEmpty();

            assertThat(profileResponse.getUsername()).isEmpty();
            assertThat(profileResponse.getEmail()).isEmpty();
            assertThat(profileResponse.getDisplayName()).isEmpty();
            assertThat(profileResponse.getAvatarUrl()).isEmpty();
        }

        @Test
        @DisplayName("Should handle UserCreation with empty strings")
        void shouldHandleUserCreationWithEmptyStrings() {
            // Given
            userCreation.setUsername("");
            userCreation.setDisplayName("");
            userCreation.setEmail("");

            // When
            User result = userMapper.toEntity(userCreation);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEmpty();
            assertThat(result.getDisplayName()).isEmpty();
            assertThat(result.getEmail()).isEmpty();
        }

        @Test
        @DisplayName("Should handle UpdateProfileRequest with empty strings")
        void shouldHandleUpdateProfileRequestWithEmptyStrings() {
            // Given
            updateRequest.setUsername("");
            updateRequest.setDisplayName("");

            // When
            userMapper.updateUserFromRequest(testUser, updateRequest);

            // Then
            assertThat(testUser.getUsername()).isEmpty();
            assertThat(testUser.getDisplayName()).isEmpty();
        }
    }
}
