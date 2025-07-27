package com.vn.caro_game.services;

import com.vn.caro_game.dtos.request.UpdateProfileRequest;
import com.vn.caro_game.dtos.response.UserProfileResponse;
import com.vn.caro_game.entities.User;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserProfileService.
 *
 * <p>This test class covers all functionality of UserProfileService including
 * profile retrieval, updates, avatar management, and error scenarios.</p>
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

    @Mock
    private MultipartFile avatarFile;

    @InjectMocks
    private UserProfileService userProfileService;

    private User testUser;
    private UpdateProfileRequest updateRequest;
    private UserProfileResponse profileResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setDisplayName("Test User");
        testUser.setAvatarUrl("/uploads/avatars/test.jpg");
        testUser.setCreatedAt(LocalDateTime.now());

        updateRequest = new UpdateProfileRequest();
        updateRequest.setUsername("newusername");
        updateRequest.setDisplayName("New Display Name");

        profileResponse = UserProfileResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .displayName("Test User")
                .avatarUrl("/uploads/avatars/test.jpg")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Get User Profile Tests")
    class GetUserProfileTests {

        @Test
        @DisplayName("Should return user profile when user exists")
        void shouldReturnUserProfileWhenUserExists() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.toProfileResponse(testUser)).thenReturn(profileResponse);

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
        void shouldThrowCustomExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userProfileService.getUserProfile(1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("User not found");

            verify(userRepository).findById(1L);
            verify(userMapper, never()).toProfileResponse(any());
        }
    }

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update profile successfully when username is unchanged")
        void shouldUpdateProfileSuccessfullyWhenUsernameUnchanged() {
            // Given
            updateRequest.setUsername("testuser"); // Same username
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toProfileResponse(testUser)).thenReturn(profileResponse);

            // When
            UserProfileResponse result = userProfileService.updateProfile(1L, updateRequest);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findById(1L);
            verify(userMapper).updateUserFromRequest(testUser, updateRequest);
            verify(userRepository).save(testUser);
            verify(userRepository, never()).existsByUsernameAndIdNot(anyString(), anyLong());
        }

        @Test
        @DisplayName("Should update profile successfully when new username is unique")
        void shouldUpdateProfileSuccessfullyWhenNewUsernameIsUnique() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsernameAndIdNot("newusername", 1L)).thenReturn(false);
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toProfileResponse(testUser)).thenReturn(profileResponse);

            // When
            UserProfileResponse result = userProfileService.updateProfile(1L, updateRequest);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findById(1L);
            verify(userRepository).existsByUsernameAndIdNot("newusername", 1L);
            verify(userMapper).updateUserFromRequest(testUser, updateRequest);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw CustomException when username already exists")
        void shouldThrowCustomExceptionWhenUsernameAlreadyExists() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsernameAndIdNot("newusername", 1L)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userProfileService.updateProfile(1L, updateRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Username already exists");

            verify(userRepository).findById(1L);
            verify(userRepository).existsByUsernameAndIdNot("newusername", 1L);
            verify(userMapper, never()).updateUserFromRequest(any(), any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when user not found")
        void shouldThrowCustomExceptionWhenUserNotFoundForUpdate() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userProfileService.updateProfile(1L, updateRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("User not found");

            verify(userRepository).findById(1L);
            verify(userMapper, never()).updateUserFromRequest(any(), any());
        }
    }

    @Nested
    @DisplayName("Update Avatar Tests")
    class UpdateAvatarTests {

        @Test
        @DisplayName("Should update avatar successfully")
        void shouldUpdateAvatarSuccessfully() {
            // Given
            String newAvatarUrl = "/uploads/avatars/new_avatar.jpg";
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(fileUploadService.uploadAvatar(avatarFile, 1L)).thenReturn(newAvatarUrl);
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toProfileResponse(testUser)).thenReturn(profileResponse);

            // When
            UserProfileResponse result = userProfileService.updateAvatar(1L, avatarFile);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findById(1L);
            verify(fileUploadService).deleteAvatar("/uploads/avatars/test.jpg");
            verify(fileUploadService).uploadAvatar(avatarFile, 1L);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should update avatar without deleting old one when no existing avatar")
        void shouldUpdateAvatarWithoutDeletingWhenNoExistingAvatar() {
            // Given
            testUser.setAvatarUrl(null);
            String newAvatarUrl = "/uploads/avatars/new_avatar.jpg";
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(fileUploadService.uploadAvatar(avatarFile, 1L)).thenReturn(newAvatarUrl);
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toProfileResponse(testUser)).thenReturn(profileResponse);

            // When
            UserProfileResponse result = userProfileService.updateAvatar(1L, avatarFile);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findById(1L);
            verify(fileUploadService, never()).deleteAvatar(anyString());
            verify(fileUploadService).uploadAvatar(avatarFile, 1L);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw CustomException when user not found")
        void shouldThrowCustomExceptionWhenUserNotFoundForAvatar() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userProfileService.updateAvatar(1L, avatarFile))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("User not found");

            verify(userRepository).findById(1L);
            verify(fileUploadService, never()).uploadAvatar(any(), any());
        }
    }

    @Nested
    @DisplayName("Update Profile With Avatar Tests")
    class UpdateProfileWithAvatarTests {

        @Test
        @DisplayName("Should update both profile and avatar successfully")
        void shouldUpdateBothProfileAndAvatarSuccessfully() {
            // Given
            String newAvatarUrl = "/uploads/avatars/new_avatar.jpg";
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsernameAndIdNot("newusername", 1L)).thenReturn(false);
            when(avatarFile.isEmpty()).thenReturn(false);
            when(fileUploadService.uploadAvatar(avatarFile, 1L)).thenReturn(newAvatarUrl);
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toProfileResponse(testUser)).thenReturn(profileResponse);

            // When
            UserProfileResponse result = userProfileService.updateProfileWithAvatar(1L, updateRequest, avatarFile);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findById(1L);
            verify(userRepository).existsByUsernameAndIdNot("newusername", 1L);
            verify(userMapper).updateUserFromRequest(testUser, updateRequest);
            verify(fileUploadService).deleteAvatar("/uploads/avatars/test.jpg");
            verify(fileUploadService).uploadAvatar(avatarFile, 1L);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should update only profile when no avatar provided")
        void shouldUpdateOnlyProfileWhenNoAvatarProvided() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsernameAndIdNot("newusername", 1L)).thenReturn(false);
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toProfileResponse(testUser)).thenReturn(profileResponse);

            // When
            UserProfileResponse result = userProfileService.updateProfileWithAvatar(1L, updateRequest, null);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findById(1L);
            verify(userRepository).existsByUsernameAndIdNot("newusername", 1L);
            verify(userMapper).updateUserFromRequest(testUser, updateRequest);
            verify(fileUploadService, never()).uploadAvatar(any(), any());
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should update only profile when empty avatar provided")
        void shouldUpdateOnlyProfileWhenEmptyAvatarProvided() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsernameAndIdNot("newusername", 1L)).thenReturn(false);
            when(avatarFile.isEmpty()).thenReturn(true);
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toProfileResponse(testUser)).thenReturn(profileResponse);

            // When
            UserProfileResponse result = userProfileService.updateProfileWithAvatar(1L, updateRequest, avatarFile);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findById(1L);
            verify(userRepository).existsByUsernameAndIdNot("newusername", 1L);
            verify(userMapper).updateUserFromRequest(testUser, updateRequest);
            verify(fileUploadService, never()).uploadAvatar(any(), any());
            verify(userRepository).save(testUser);
        }
    }
}
