package com.vn.caro_game.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.caro_game.configs.CustomUserDetails;
import com.vn.caro_game.dtos.request.UpdateProfileRequest;
import com.vn.caro_game.dtos.response.UserProfileResponse;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.StatusCode;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.repositories.UserRepository;
import com.vn.caro_game.services.interfaces.IUserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for UserProfileController.
 *
 * <p>This test class follows clean code principles and testing best practices:
 * <ul>
 *   <li>Clear test structure with nested classes for logical grouping</li>
 *   <li>Descriptive test names that explain the scenario and expected outcome</li>
 *   <li>Proper mocking and isolation of dependencies</li>
 *   <li>Comprehensive coverage of success and error scenarios</li>
 *   <li>Validation of both HTTP status codes and response content</li>
 * </ul>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UserProfileController Tests")
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IUserProfileService userProfileService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private CustomUserDetails testUserDetails;
    private UserProfileResponse testUserProfileResponse;
    private UpdateProfileRequest testUpdateRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setDisplayName("Test User");
        testUser.setAvatarUrl("/uploads/avatars/test_avatar.jpg");
        testUser.setCreatedAt(LocalDateTime.now());

        // Setup CustomUserDetails
        testUserDetails = new CustomUserDetails(testUser);

        // Setup UserProfileResponse
        testUserProfileResponse = UserProfileResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .displayName("Test User")
                .avatarUrl("/uploads/avatars/test_avatar.jpg")
                .createdAt(LocalDateTime.now())
                .build();

        // Setup UpdateProfileRequest
        testUpdateRequest = UpdateProfileRequest.builder()
                .username("updateduser")
                .displayName("Updated User")
                .build();
    }

    @Nested
    @DisplayName("GET /api/user-profile - Get User Profile")
    class GetUserProfileTests {

        @Test
        @DisplayName("Should return user profile successfully when user exists")
        void shouldReturnUserProfileSuccessfully() throws Exception {
            // Given
            when(userProfileService.getUserProfile(1L)).thenReturn(testUserProfileResponse);

            // When & Then
            mockMvc.perform(get("/api/user-profile")
                    .with(user(testUserDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User profile retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.username").value("testuser"))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"))
                    .andExpect(jsonPath("$.data.displayName").value("Test User"));

            verify(userProfileService).getUserProfile(1L);
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Given
            when(userProfileService.getUserProfile(1L))
                    .thenThrow(new CustomException("User not found", StatusCode.USER_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/user-profile")
                    .with(user(testUserDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(userProfileService).getUserProfile(1L);
        }

        @Test
        @DisplayName("Should return 401 when user not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/user-profile")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());

            verify(userProfileService, never()).getUserProfile(anyLong());
        }
    }

    @Nested
    @DisplayName("PUT /api/user-profile - Update User Profile")
    class UpdateUserProfileTests {

        @Test
        @DisplayName("Should update user profile successfully with valid data")
        void shouldUpdateUserProfileSuccessfully() throws Exception {
            // Given
            UserProfileResponse updatedResponse = UserProfileResponse.builder()
                    .id(1L)
                    .username("updateduser")
                    .email("updated@example.com")
                    .displayName("Updated User")
                    .avatarUrl("/uploads/avatars/test_avatar.jpg")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userProfileService.updateProfile(eq(1L), any(UpdateProfileRequest.class)))
                    .thenReturn(updatedResponse);

            // When & Then
            mockMvc.perform(put("/api/user-profile")
                    .with(user(testUserDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Profile updated successfully"))
                    .andExpect(jsonPath("$.data.username").value("updateduser"))
                    .andExpect(jsonPath("$.data.email").value("updated@example.com"))
                    .andExpect(jsonPath("$.data.displayName").value("Updated User"));

            verify(userProfileService).updateProfile(eq(1L), any(UpdateProfileRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when validation fails")
        void shouldReturn400WhenValidationFails() throws Exception {
            // Given - Invalid request with empty username
            UpdateProfileRequest invalidRequest = UpdateProfileRequest.builder()
                    .username("") // Invalid - empty username
                    .displayName("") // Invalid - empty display name
                    .build();

            // When & Then
            mockMvc.perform(put("/api/user-profile")
                    .with(user(testUserDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(userProfileService, never()).updateProfile(anyLong(), any(UpdateProfileRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when username already exists")
        void shouldReturn400WhenUsernameAlreadyExists() throws Exception {
            // Given
            when(userProfileService.updateProfile(eq(1L), any(UpdateProfileRequest.class)))
                    .thenThrow(new CustomException("Username already exists", StatusCode.USERNAME_ALREADY_EXISTS));

            // When & Then
            mockMvc.perform(put("/api/user-profile")
                    .with(user(testUserDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testUpdateRequest)))
                    .andExpect(status().isBadRequest());

            verify(userProfileService).updateProfile(eq(1L), any(UpdateProfileRequest.class));
        }
    }

    @Nested
    @DisplayName("POST /api/user-profile/avatar - Upload Avatar")
    class UploadAvatarTests {

        @Test
        @DisplayName("Should upload avatar successfully with valid image file")
        void shouldUploadAvatarSuccessfully() throws Exception {
            // Given
            MockMultipartFile avatarFile = new MockMultipartFile(
                    "avatar",
                    "test-avatar.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );

            UserProfileResponse updatedResponse = UserProfileResponse.builder()
                    .id(1L)
                    .username("testuser")
                    .email("test@example.com")
                    .displayName("Test User")
                    .avatarUrl("/uploads/avatars/user_1_avatar_new.jpg")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userProfileService.updateAvatar(eq(1L), any())).thenReturn(updatedResponse);

            // When & Then
            mockMvc.perform(multipart("/api/user-profile/avatar")
                    .file(avatarFile)
                    .with(user(testUserDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Avatar uploaded successfully"))
                    .andExpect(jsonPath("$.data.avatarUrl").value("/uploads/avatars/user_1_avatar_new.jpg"));

            verify(userProfileService).updateAvatar(eq(1L), any());
        }

        @Test
        @DisplayName("Should return 400 when file is too large")
        void shouldReturn400WhenFileIsTooLarge() throws Exception {
            // Given
            MockMultipartFile largeFile = new MockMultipartFile(
                    "avatar",
                    "large-avatar.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    new byte[6 * 1024 * 1024] // 6MB - exceeds 5MB limit
            );

            when(userProfileService.updateAvatar(eq(1L), any()))
                    .thenThrow(new CustomException("File size exceeds maximum limit", StatusCode.FILE_TOO_LARGE));

            // When & Then
            mockMvc.perform(multipart("/api/user-profile/avatar")
                    .file(largeFile)
                    .with(user(testUserDetails)))
                    .andExpect(status().isBadRequest());

            verify(userProfileService).updateAvatar(eq(1L), any());
        }

        @Test
        @DisplayName("Should return 400 when file format is invalid")
        void shouldReturn400WhenFileFormatIsInvalid() throws Exception {
            // Given
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "avatar",
                    "document.pdf",
                    "application/pdf",
                    "pdf content".getBytes()
            );

            when(userProfileService.updateAvatar(eq(1L), any()))
                    .thenThrow(new CustomException("Invalid file type", StatusCode.INVALID_FILE_TYPE));

            // When & Then
            mockMvc.perform(multipart("/api/user-profile/avatar")
                    .file(invalidFile)
                    .with(user(testUserDetails)))
                    .andExpect(status().isBadRequest());

            verify(userProfileService).updateAvatar(eq(1L), any());
        }
    }

    @Nested
    @DisplayName("PUT /api/user-profile/complete - Update Complete Profile")
    class UpdateCompleteProfileTests {

        @Test
        @DisplayName("Should update both profile and avatar successfully")
        void shouldUpdateCompleteProfileSuccessfully() throws Exception {
            // Given
            MockMultipartFile avatarFile = new MockMultipartFile(
                    "avatar",
                    "new-avatar.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "new avatar content".getBytes()
            );

            UserProfileResponse updatedResponse = UserProfileResponse.builder()
                    .id(1L)
                    .username("updateduser")
                    .email("updated@example.com")
                    .displayName("Updated User")
                    .avatarUrl("/uploads/avatars/user_1_avatar_updated.jpg")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userProfileService.updateProfileWithAvatar(eq(1L), any(UpdateProfileRequest.class), any()))
                    .thenReturn(updatedResponse);

            // When & Then
            mockMvc.perform(multipart("/api/user-profile/complete")
                    .file(avatarFile)
                    .param("username", "updateduser")
                    .param("email", "updated@example.com")
                    .param("displayName", "Updated User")
                    .with(user(testUserDetails))
                    .with(request -> {
                        request.setMethod("PUT");
                        return request;
                    }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Profile updated successfully"))
                    .andExpect(jsonPath("$.data.username").value("updateduser"))
                    .andExpect(jsonPath("$.data.avatarUrl").value("/uploads/avatars/user_1_avatar_updated.jpg"));

            verify(userProfileService).updateProfileWithAvatar(eq(1L), any(UpdateProfileRequest.class), any());
        }

        @Test
        @DisplayName("Should update profile without avatar when avatar not provided")
        void shouldUpdateProfileWithoutAvatar() throws Exception {
            // Given
            UserProfileResponse updatedResponse = UserProfileResponse.builder()
                    .id(1L)
                    .username("updateduser")
                    .email("updated@example.com")
                    .displayName("Updated User")
                    .avatarUrl("/uploads/avatars/test_avatar.jpg") // Same avatar
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userProfileService.updateProfileWithAvatar(eq(1L), any(UpdateProfileRequest.class), isNull()))
                    .thenReturn(updatedResponse);

            // When & Then
            mockMvc.perform(multipart("/api/user-profile/complete")
                    .param("username", "updateduser")
                    .param("email", "updated@example.com")
                    .param("displayName", "Updated User")
                    .with(user(testUserDetails))
                    .with(request -> {
                        request.setMethod("PUT");
                        return request;
                    }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.username").value("updateduser"))
                    .andExpect(jsonPath("$.data.avatarUrl").value("/uploads/avatars/test_avatar.jpg"));

            verify(userProfileService).updateProfileWithAvatar(eq(1L), any(UpdateProfileRequest.class), isNull());
        }
    }
}
