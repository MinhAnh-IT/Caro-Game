package com.vn.caro_game.controllers;

import com.vn.caro_game.dtos.request.UpdateProfileRequest;
import com.vn.caro_game.dtos.response.ApiResponse;
import com.vn.caro_game.dtos.response.UserProfileResponse;
import com.vn.caro_game.integrations.jwt.JwtService;
import com.vn.caro_game.services.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserProfileController.
 *
 * <p>This test class covers all REST endpoints for user profile management
 * using pure unit testing approach.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserProfileController Tests")
class UserProfileControllerTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private UserProfileController userProfileController;

    private UserProfileResponse profileResponse;
    private UpdateProfileRequest updateRequest;
    private final String JWT_TOKEN = "valid.jwt.token";
    private final String AUTH_HEADER = "Bearer " + JWT_TOKEN;

    @BeforeEach
    void setUp() {
        profileResponse = UserProfileResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .displayName("Test User")
                .avatarUrl("/uploads/avatars/test.jpg")
                .createdAt(LocalDateTime.now())
                .build();

        updateRequest = new UpdateProfileRequest();
        updateRequest.setUsername("newusername");
        updateRequest.setDisplayName("New Display Name");

        // Mock HttpServletRequest with Authorization header
        when(httpServletRequest.getHeader("Authorization")).thenReturn(AUTH_HEADER);
        when(jwtService.getUserIdFromToken(JWT_TOKEN)).thenReturn(1L);
    }

    @Nested
    @DisplayName("Get User Profile Tests")
    class GetUserProfileTests {

        @Test
        @DisplayName("Should return user profile successfully")
        void shouldReturnUserProfileSuccessfully() {
            // Given
            when(userProfileService.getUserProfile(1L)).thenReturn(profileResponse);

            // When
            ResponseEntity<ApiResponse<UserProfileResponse>> response = userProfileController.getUserProfile(httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Profile retrieved successfully");
            assertThat(response.getBody().getData()).isEqualTo(profileResponse);

            verify(userProfileService).getUserProfile(1L);
        }

        @Test
        @DisplayName("Should throw exception when no authorization header")
        void shouldThrowExceptionWhenNoAuthorizationHeader() {
            // Given
            when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> userProfileController.getUserProfile(httpServletRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No valid JWT token found");

            verify(userProfileService, never()).getUserProfile(anyLong());
        }

        @Test
        @DisplayName("Should throw exception when invalid authorization header")
        void shouldThrowExceptionWhenInvalidAuthorizationHeader() {
            // Given
            when(httpServletRequest.getHeader("Authorization")).thenReturn("Invalid Token");

            // When & Then
            assertThatThrownBy(() -> userProfileController.getUserProfile(httpServletRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No valid JWT token found");

            verify(userProfileService, never()).getUserProfile(anyLong());
        }
    }

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update profile successfully")
        void shouldUpdateProfileSuccessfully() {
            // Given
            when(userProfileService.updateProfile(eq(1L), any(UpdateProfileRequest.class)))
                    .thenReturn(profileResponse);

            // When
            ResponseEntity<ApiResponse<UserProfileResponse>> response =
                userProfileController.updateProfile(httpServletRequest, updateRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Profile updated successfully");
            assertThat(response.getBody().getData().getUsername()).isEqualTo("testuser");

            verify(userProfileService).updateProfile(eq(1L), any(UpdateProfileRequest.class));
        }

        @Test
        @DisplayName("Should handle service exception for invalid username")
        void shouldHandleServiceExceptionForInvalidUsername() {
            // Given
            updateRequest.setUsername("invalid");
            when(userProfileService.updateProfile(eq(1L), any(UpdateProfileRequest.class)))
                    .thenThrow(new RuntimeException("Username validation failed"));

            // When & Then
            assertThatThrownBy(() ->
                userProfileController.updateProfile(httpServletRequest, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username validation failed");

            verify(userProfileService).updateProfile(eq(1L), any(UpdateProfileRequest.class));
        }

        @Test
        @DisplayName("Should handle service exception for duplicate username")
        void shouldHandleServiceExceptionForDuplicateUsername() {
            // Given
            when(userProfileService.updateProfile(eq(1L), any(UpdateProfileRequest.class)))
                    .thenThrow(new RuntimeException("Username already exists"));

            // When & Then
            assertThatThrownBy(() ->
                userProfileController.updateProfile(httpServletRequest, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");

            verify(userProfileService).updateProfile(eq(1L), any(UpdateProfileRequest.class));
        }

        @Test
        @DisplayName("Should handle service exception for invalid display name")
        void shouldHandleServiceExceptionForInvalidDisplayName() {
            // Given
            updateRequest.setDisplayName("a".repeat(51));
            when(userProfileService.updateProfile(eq(1L), any(UpdateProfileRequest.class)))
                    .thenThrow(new RuntimeException("Display name too long"));

            // When & Then
            assertThatThrownBy(() ->
                userProfileController.updateProfile(httpServletRequest, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Display name too long");

            verify(userProfileService).updateProfile(eq(1L), any(UpdateProfileRequest.class));
        }
    }

    @Nested
    @DisplayName("Update Avatar Tests")
    class UpdateAvatarTests {

        @Test
        @DisplayName("Should update avatar successfully")
        void shouldUpdateAvatarSuccessfully() {
            // Given
            MockMultipartFile avatarFile = new MockMultipartFile(
                    "avatar",
                    "test-avatar.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            when(jwtService.getUserIdFromToken(JWT_TOKEN)).thenReturn(1L);
            when(userProfileService.updateAvatar(eq(1L), any())).thenReturn(profileResponse);

            // When
            ResponseEntity<ApiResponse<UserProfileResponse>> response = userProfileController.updateAvatar(httpServletRequest, avatarFile);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Avatar updated successfully");
            assertThat(response.getBody().getData().getUsername()).isEqualTo("testuser");

            verify(userProfileService).updateAvatar(eq(1L), any());
        }

        @Test
        @DisplayName("Should return 400 when no file provided")
        void shouldReturn400WhenNoFileProvided() {
            // When
            ResponseEntity<ApiResponse<UserProfileResponse>> response = userProfileController.updateAvatar(httpServletRequest, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();

            verify(userProfileService, never()).updateAvatar(anyLong(), any());
        }
    }

    @Nested
    @DisplayName("Update Complete Profile Tests")
    class UpdateCompleteProfileTests {

        @Test
        @DisplayName("Should update complete profile successfully with avatar")
        void shouldUpdateCompleteProfileSuccessfullyWithAvatar() {
            // Given
            MockMultipartFile avatarFile = new MockMultipartFile(
                    "avatar",
                    "test-avatar.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            when(jwtService.getUserIdFromToken(JWT_TOKEN)).thenReturn(1L);
            when(userProfileService.updateProfileWithAvatar(eq(1L), any(), any()))
                    .thenReturn(profileResponse);

            // When
            ResponseEntity<ApiResponse<UserProfileResponse>> response = userProfileController.updateProfileComplete(
                    httpServletRequest, "newusername", "New Display Name", avatarFile);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Profile updated successfully");
            assertThat(response.getBody().getData().getUsername()).isEqualTo("testuser");

            verify(userProfileService).updateProfileWithAvatar(eq(1L), any(), any());
        }

        @Test
        @DisplayName("Should update complete profile successfully without avatar")
        void shouldUpdateCompleteProfileSuccessfullyWithoutAvatar() {
            // Given
            when(jwtService.getUserIdFromToken(JWT_TOKEN)).thenReturn(1L);
            when(userProfileService.updateProfileWithAvatar(eq(1L), any(), eq(null)))
                    .thenReturn(profileResponse);

            // When
            ResponseEntity<ApiResponse<UserProfileResponse>> response = userProfileController.updateProfileComplete(
                    httpServletRequest, "newusername", "New Display Name", null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Profile updated successfully");

            verify(userProfileService).updateProfileWithAvatar(eq(1L), any(), eq(null));
        }
    }
}
