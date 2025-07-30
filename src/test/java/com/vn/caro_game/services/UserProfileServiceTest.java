package com.vn.caro_game.services;

import com.vn.caro_game.dtos.request.UpdateProfileRequest;
import com.vn.caro_game.dtos.response.UserProfileResponse;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.StatusCode;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.mappers.UserMapper;
import com.vn.caro_game.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for UserProfileService.
 *
 * <p>Tests all business logic scenarios including success cases, validation failures,
 * and error conditions. Follows clean code and testing best practices.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileService Tests")
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private UserProfileService userProfileService;

    private User testUser;
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
    @DisplayName("Get User Profile Tests")
    class GetUserProfileTests {

        @Test
        @DisplayName("Should return user profile successfully when user exists")
        void shouldReturnUserProfileSuccessfully() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.toProfileResponse(testUser)).thenReturn(testUserProfileResponse);

            // When
            UserProfileResponse result = userProfileService.getUserProfile(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getDisplayName()).isEqualTo("Test User");

            verify(userRepository).findById(1L);
            verify(userMapper).toProfileResponse(testUser);
        }

        @Test
        @DisplayName("Should throw CustomException when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userProfileService.getUserProfile(1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("User not found")
                    .extracting("statusCode")
                    .isEqualTo(StatusCode.USER_NOT_FOUND);

            verify(userRepository).findById(1L);
            verify(userMapper, never()).toProfileResponse(any());
        }
    }

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update profile successfully when username unchanged")
        void shouldUpdateProfileSuccessfullyWhenUsernameUnchanged() {
            // Given
            testUpdateRequest.setUsername("testuser"); // Same username

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toProfileResponse(testUser)).thenReturn(testUserProfileResponse);

            // When
            UserProfileResponse result = userProfileService.updateProfile(1L, testUpdateRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);

            verify(userRepository).findById(1L);
            verify(userMapper).updateUserFromRequest(testUser, testUpdateRequest);
            verify(userRepository).save(testUser);
            verify(userMapper).toProfileResponse(testUser);
            // Should not check username uniqueness since username unchanged
            verify(userRepository, never()).existsByUsernameAndIdNot(anyString(), anyLong());
        }

        @Test
        @DisplayName("Should update profile successfully when new username is unique")
        void shouldUpdateProfileSuccessfullyWhenNewUsernameIsUnique() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsernameAndIdNot("updateduser", 1L)).thenReturn(false);
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toProfileResponse(testUser)).thenReturn(testUserProfileResponse);

            // When
            UserProfileResponse result = userProfileService.updateProfile(1L, testUpdateRequest);

            // Then
            assertThat(result).isNotNull();

            verify(userRepository).findById(1L);
            verify(userRepository).existsByUsernameAndIdNot("updateduser", 1L);
            verify(userMapper).updateUserFromRequest(testUser, testUpdateRequest);
            verify(userRepository).save(testUser);
            verify(userMapper).toProfileResponse(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFoundForUpdate() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userProfileService.updateProfile(1L, testUpdateRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("User not found")
                    .extracting("statusCode")
                    .isEqualTo(StatusCode.USER_NOT_FOUND);

            verify(userRepository).findById(1L);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void shouldThrowExceptionWhenUsernameAlreadyExists() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsernameAndIdNot("updateduser", 1L)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userProfileService.updateProfile(1L, testUpdateRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("Username already exists")
                    .extracting("statusCode")
                    .isEqualTo(StatusCode.USERNAME_ALREADY_EXISTS);

            verify(userRepository).findById(1L);
            verify(userRepository).existsByUsernameAndIdNot("updateduser", 1L);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Avatar Tests")
    class UpdateAvatarTests {

        private MultipartFile mockAvatarFile;

        @BeforeEach
        void setUp() {
            mockAvatarFile = new MockMultipartFile(
                    "avatar",
                    "test-avatar.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );
        }

        @Test
        @DisplayName("Should update avatar successfully for user without existing avatar")
        void shouldUpdateAvatarSuccessfullyForUserWithoutExistingAvatar() {
            // Given
            testUser.setAvatarUrl(null); // No existing avatar
            String newAvatarUrl = "/uploads/avatars/user_1_avatar_new.jpg";

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(fileUploadService.uploadAvatar(mockAvatarFile, 1L)).thenReturn(newAvatarUrl);
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toProfileResponse(testUser)).thenReturn(testUserProfileResponse);

            // When
            UserProfileResponse result = userProfileService.updateAvatar(1L, mockAvatarFile);

            // Then
            assertThat(result).isNotNull();

            verify(userRepository).findById(1L);
            verify(fileUploadService, never()).deleteAvatar(anyString()); // No existing avatar to delete
            verify(fileUploadService).uploadAvatar(mockAvatarFile, 1L);
            verify(userRepository).save(testUser);
            verify(userMapper).toProfileResponse(testUser);
        }

        @Test
        @DisplayName("Should update avatar successfully and delete old avatar when user has existing avatar")
        void shouldUpdateAvatarSuccessfullyAndDeleteOldAvatar() {
            // Given
            String oldAvatarUrl = "/uploads/avatars/old_avatar.jpg";
            String newAvatarUrl = "/uploads/avatars/user_1_avatar_new.jpg";
            testUser.setAvatarUrl(oldAvatarUrl);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(fileUploadService.uploadAvatar(mockAvatarFile, 1L)).thenReturn(newAvatarUrl);
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toProfileResponse(testUser)).thenReturn(testUserProfileResponse);

            // When
            UserProfileResponse result = userProfileService.updateAvatar(1L, mockAvatarFile);

            // Then
            assertThat(result).isNotNull();

            verify(userRepository).findById(1L);
            verify(fileUploadService).deleteAvatar(oldAvatarUrl); // Should delete old avatar
            verify(fileUploadService).uploadAvatar(mockAvatarFile, 1L);
            verify(userRepository).save(testUser);
            verify(userMapper).toProfileResponse(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found for avatar update")
        void shouldThrowExceptionWhenUserNotFoundForAvatarUpdate() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userProfileService.updateAvatar(1L, mockAvatarFile))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("User not found")
                    .extracting("statusCode")
                    .isEqualTo(StatusCode.USER_NOT_FOUND);

            verify(userRepository).findById(1L);
            verify(fileUploadService, never()).uploadAvatar(any(), anyLong());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Profile With Avatar Tests")
    class UpdateProfileWithAvatarTests {

        private MultipartFile mockAvatarFile;

        @BeforeEach
        void setUp() {
            mockAvatarFile = new MockMultipartFile(
                    "avatar",
                    "test-avatar.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );
        }

        @Test
        @DisplayName("Should update both profile and avatar successfully")
        void shouldUpdateBothProfileAndAvatarSuccessfully() {
            // Given
            String oldAvatarUrl = "/uploads/avatars/old_avatar.jpg";
            String newAvatarUrl = "/uploads/avatars/user_1_avatar_new.jpg";
            testUser.setAvatarUrl(oldAvatarUrl);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsernameAndIdNot("updateduser", 1L)).thenReturn(false);
            when(fileUploadService.uploadAvatar(mockAvatarFile, 1L)).thenReturn(newAvatarUrl);
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toProfileResponse(testUser)).thenReturn(testUserProfileResponse);

            // When
            UserProfileResponse result = userProfileService.updateProfileWithAvatar(1L, testUpdateRequest, mockAvatarFile);

            // Then
            assertThat(result).isNotNull();

            verify(userRepository).findById(1L);
            verify(userRepository).existsByUsernameAndIdNot("updateduser", 1L);
            verify(userMapper).updateUserFromRequest(testUser, testUpdateRequest);
            verify(fileUploadService).deleteAvatar(oldAvatarUrl);
            verify(fileUploadService).uploadAvatar(mockAvatarFile, 1L);
            verify(userRepository).save(testUser);
            verify(userMapper).toProfileResponse(testUser);
        }

        @Test
        @DisplayName("Should update only profile when avatar file is null")
        void shouldUpdateOnlyProfileWhenAvatarFileIsNull() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsernameAndIdNot("updateduser", 1L)).thenReturn(false);
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toProfileResponse(testUser)).thenReturn(testUserProfileResponse);

            // When
            UserProfileResponse result = userProfileService.updateProfileWithAvatar(1L, testUpdateRequest, null);

            // Then
            assertThat(result).isNotNull();

            verify(userRepository).findById(1L);
            verify(userRepository).existsByUsernameAndIdNot("updateduser", 1L);
            verify(userMapper).updateUserFromRequest(testUser, testUpdateRequest);
            verify(fileUploadService, never()).deleteAvatar(anyString());
            verify(fileUploadService, never()).uploadAvatar(any(), anyLong());
            verify(userRepository).save(testUser);
            verify(userMapper).toProfileResponse(testUser);
        }

        @Test
        @DisplayName("Should update only profile when avatar file is empty")
        void shouldUpdateOnlyProfileWhenAvatarFileIsEmpty() {
            // Given
            MockMultipartFile emptyFile = new MockMultipartFile("avatar", "", "image/jpeg", new byte[0]);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsernameAndIdNot("updateduser", 1L)).thenReturn(false);
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toProfileResponse(testUser)).thenReturn(testUserProfileResponse);

            // When
            UserProfileResponse result = userProfileService.updateProfileWithAvatar(1L, testUpdateRequest, emptyFile);

            // Then
            assertThat(result).isNotNull();

            verify(userRepository).findById(1L);
            verify(userRepository).existsByUsernameAndIdNot("updateduser", 1L);
            verify(userMapper).updateUserFromRequest(testUser, testUpdateRequest);
            verify(fileUploadService, never()).deleteAvatar(anyString());
            verify(fileUploadService, never()).uploadAvatar(any(), anyLong());
            verify(userRepository).save(testUser);
            verify(userMapper).toProfileResponse(testUser);
        }
    }
}
