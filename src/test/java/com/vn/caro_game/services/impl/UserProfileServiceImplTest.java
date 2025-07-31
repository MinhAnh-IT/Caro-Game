package com.vn.caro_game.services.impl;

import com.vn.caro_game.constants.UserProfileConstants;
import com.vn.caro_game.dtos.request.UpdateProfileRequest;
import com.vn.caro_game.dtos.response.UserProfileResponse;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.mappers.UserMapper;
import com.vn.caro_game.repositories.UserRepository;
import com.vn.caro_game.services.interfaces.IFileUploadService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

/**
 * Unit tests for UserProfileServiceImpl.
 *
 * <p>This test class verifies the business logic of user profile operations
 * including profile retrieval, updates, and avatar management. It uses mocks
 * to isolate the service layer and test only the business logic.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileServiceImpl Tests")
class UserProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private IFileUploadService fileUploadService;

    @Mock
    private MultipartFile avatarFile;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private User testUser;
    private UserProfileResponse testResponse;
    private UpdateProfileRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("john_doe");
        testUser.setEmail("john@example.com");
        testUser.setDisplayName("John Doe");
        testUser.setAvatarUrl("/uploads/avatars/user_1_avatar.jpg");
        testUser.setCreatedAt(LocalDateTime.now());

        testResponse = UserProfileResponse.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .displayName("John Doe")
                .avatarUrl("/uploads/avatars/user_1_avatar.jpg")
                .createdAt(testUser.getCreatedAt())
                .build();

        updateRequest = UpdateProfileRequest.builder()
                .username("john_doe_updated")
                .displayName("John Doe Updated")
                .build();
    }

    @Nested
    @DisplayName("Get User Profile Tests")
    class GetUserProfileTests {

        @Test
        @DisplayName("Should successfully return user profile when user exists")
        void shouldReturnUserProfileWhenUserExists() {
            // Given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(userMapper.toProfileResponse(testUser)).willReturn(testResponse);

            // When
            UserProfileResponse result = userProfileService.getUserProfile(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("john_doe");
            assertThat(result.getEmail()).isEqualTo("john@example.com");

            then(userRepository).should().findById(1L);
            then(userMapper).should().toProfileResponse(testUser);
        }

        @Test
        @DisplayName("Should throw CustomException when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userProfileService.getUserProfile(999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UserProfileConstants.USER_NOT_FOUND);

            then(userRepository).should().findById(999L);
            then(userMapper).should(never()).toProfileResponse(any());
        }
    }

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should successfully update profile with new username")
        void shouldUpdateProfileWithNewUsername() {
            // Given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(userRepository.existsByUsernameAndIdNot("john_doe_updated", 1L)).willReturn(false);
            given(userRepository.save(testUser)).willReturn(testUser);
            given(userMapper.toProfileResponse(testUser)).willReturn(testResponse);
            willDoNothing().given(userMapper).updateUserFromRequest(testUser, updateRequest);

            // When
            UserProfileResponse result = userProfileService.updateProfile(1L, updateRequest);

            // Then
            assertThat(result).isNotNull();

            then(userRepository).should().findById(1L);
            then(userRepository).should().existsByUsernameAndIdNot("john_doe_updated", 1L);
            then(userMapper).should().updateUserFromRequest(testUser, updateRequest);
            then(userRepository).should().save(testUser);
            then(userMapper).should().toProfileResponse(testUser);
        }

        @Test
        @DisplayName("Should update profile without username validation when username unchanged")
        void shouldUpdateProfileWhenUsernameUnchanged() {
            // Given
            updateRequest.setUsername("john_doe"); // Same as current username
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(userRepository.save(testUser)).willReturn(testUser);
            given(userMapper.toProfileResponse(testUser)).willReturn(testResponse);
            willDoNothing().given(userMapper).updateUserFromRequest(testUser, updateRequest);

            // When
            UserProfileResponse result = userProfileService.updateProfile(1L, updateRequest);

            // Then
            assertThat(result).isNotNull();

            then(userRepository).should().findById(1L);
            then(userRepository).should(never()).existsByUsernameAndIdNot(anyString(), anyLong());
            then(userMapper).should().updateUserFromRequest(testUser, updateRequest);
            then(userRepository).should().save(testUser);
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // Given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(userRepository.existsByUsernameAndIdNot("john_doe_updated", 1L)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> userProfileService.updateProfile(1L, updateRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UserProfileConstants.USERNAME_ALREADY_EXISTS);

            then(userRepository).should().findById(1L);
            then(userRepository).should().existsByUsernameAndIdNot("john_doe_updated", 1L);
            then(userMapper).should(never()).updateUserFromRequest(any(), any());
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFoundForUpdate() {
            // Given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userProfileService.updateProfile(999L, updateRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UserProfileConstants.USER_NOT_FOUND);

            then(userRepository).should().findById(999L);
            then(userRepository).should(never()).existsByUsernameAndIdNot(anyString(), anyLong());
        }
    }

    @Nested
    @DisplayName("Update Avatar Tests")
    class UpdateAvatarTests {

        @Test
        @DisplayName("Should successfully update avatar and delete old one")
        void shouldUpdateAvatarAndDeleteOldOne() {
            // Given
            String newAvatarUrl = "/uploads/avatars/user_1_new_avatar.jpg";
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(fileUploadService.uploadAvatar(avatarFile, 1L)).willReturn(newAvatarUrl);
            given(userRepository.save(testUser)).willReturn(testUser);
            given(userMapper.toProfileResponse(testUser)).willReturn(testResponse);
            willDoNothing().given(fileUploadService).deleteAvatar("/uploads/avatars/user_1_avatar.jpg");

            // When
            UserProfileResponse result = userProfileService.updateAvatar(1L, avatarFile);

            // Then
            assertThat(result).isNotNull();

            then(userRepository).should().findById(1L);
            then(fileUploadService).should().deleteAvatar("/uploads/avatars/user_1_avatar.jpg");
            then(fileUploadService).should().uploadAvatar(avatarFile, 1L);
            then(userRepository).should().save(testUser);
            then(userMapper).should().toProfileResponse(testUser);
        }

        @Test
        @DisplayName("Should update avatar without deleting when no old avatar exists")
        void shouldUpdateAvatarWhenNoOldAvatarExists() {
            // Given
            testUser.setAvatarUrl(null);
            String newAvatarUrl = "/uploads/avatars/user_1_new_avatar.jpg";
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(fileUploadService.uploadAvatar(avatarFile, 1L)).willReturn(newAvatarUrl);
            given(userRepository.save(testUser)).willReturn(testUser);
            given(userMapper.toProfileResponse(testUser)).willReturn(testResponse);

            // When
            UserProfileResponse result = userProfileService.updateAvatar(1L, avatarFile);

            // Then
            assertThat(result).isNotNull();

            then(userRepository).should().findById(1L);
            then(fileUploadService).should(never()).deleteAvatar(anyString());
            then(fileUploadService).should().uploadAvatar(avatarFile, 1L);
            then(userRepository).should().save(testUser);
        }

        @Test
        @DisplayName("Should throw exception when file upload fails")
        void shouldThrowExceptionWhenFileUploadFails() {
            // Given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            willThrow(new RuntimeException("Upload failed")).given(fileUploadService).uploadAvatar(avatarFile, 1L);

            // When & Then
            assertThatThrownBy(() -> userProfileService.updateAvatar(1L, avatarFile))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UserProfileConstants.AVATAR_UPDATE_FAILED);

            then(userRepository).should().findById(1L);
            then(fileUploadService).should().uploadAvatar(avatarFile, 1L);
            then(userRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Complete Profile Tests")
    class UpdateCompleteProfileTests {

        @Test
        @DisplayName("Should successfully update both profile and avatar")
        void shouldUpdateBothProfileAndAvatar() {
            // Given
            String newAvatarUrl = "/uploads/avatars/user_1_new_avatar.jpg";
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(userRepository.existsByUsernameAndIdNot("john_doe_updated", 1L)).willReturn(false);
            given(fileUploadService.uploadAvatar(avatarFile, 1L)).willReturn(newAvatarUrl);
            given(userRepository.save(testUser)).willReturn(testUser);
            given(userMapper.toProfileResponse(testUser)).willReturn(testResponse);
            given(avatarFile.isEmpty()).willReturn(false);
            willDoNothing().given(userMapper).updateUserFromRequest(testUser, updateRequest);
            willDoNothing().given(fileUploadService).deleteAvatar("/uploads/avatars/user_1_avatar.jpg");

            // When
            UserProfileResponse result = userProfileService.updateProfileWithAvatar(1L, updateRequest, avatarFile);

            // Then
            assertThat(result).isNotNull();

            then(userRepository).should().findById(1L);
            then(userRepository).should().existsByUsernameAndIdNot("john_doe_updated", 1L);
            then(userMapper).should().updateUserFromRequest(testUser, updateRequest);
            then(fileUploadService).should().deleteAvatar("/uploads/avatars/user_1_avatar.jpg");
            then(fileUploadService).should().uploadAvatar(avatarFile, 1L);
            then(userRepository).should().save(testUser);
        }

        @Test
        @DisplayName("Should update only profile when no avatar provided")
        void shouldUpdateOnlyProfileWhenNoAvatarProvided() {
            // Given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(userRepository.existsByUsernameAndIdNot("john_doe_updated", 1L)).willReturn(false);
            given(userRepository.save(testUser)).willReturn(testUser);
            given(userMapper.toProfileResponse(testUser)).willReturn(testResponse);
            willDoNothing().given(userMapper).updateUserFromRequest(testUser, updateRequest);

            // When
            UserProfileResponse result = userProfileService.updateProfileWithAvatar(1L, updateRequest, null);

            // Then
            assertThat(result).isNotNull();

            then(userRepository).should().findById(1L);
            then(userRepository).should().existsByUsernameAndIdNot("john_doe_updated", 1L);
            then(userMapper).should().updateUserFromRequest(testUser, updateRequest);
            then(fileUploadService).should(never()).deleteAvatar(anyString());
            then(fileUploadService).should(never()).uploadAvatar(any(), anyLong());
            then(userRepository).should().save(testUser);
        }

        @Test
        @DisplayName("Should update only profile when empty avatar provided")
        void shouldUpdateOnlyProfileWhenEmptyAvatarProvided() {
            // Given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(userRepository.existsByUsernameAndIdNot("john_doe_updated", 1L)).willReturn(false);
            given(userRepository.save(testUser)).willReturn(testUser);
            given(userMapper.toProfileResponse(testUser)).willReturn(testResponse);
            given(avatarFile.isEmpty()).willReturn(true);
            willDoNothing().given(userMapper).updateUserFromRequest(testUser, updateRequest);

            // When
            UserProfileResponse result = userProfileService.updateProfileWithAvatar(1L, updateRequest, avatarFile);

            // Then
            assertThat(result).isNotNull();

            then(userRepository).should().findById(1L);
            then(userRepository).should().existsByUsernameAndIdNot("john_doe_updated", 1L);
            then(userMapper).should().updateUserFromRequest(testUser, updateRequest);
            then(fileUploadService).should(never()).deleteAvatar(anyString());
            then(fileUploadService).should(never()).uploadAvatar(any(), anyLong());
            then(userRepository).should().save(testUser);
        }
    }
}
