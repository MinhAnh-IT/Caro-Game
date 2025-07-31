package com.vn.caro_game.services.impl;

import com.vn.caro_game.constants.UserProfileConstants;
import com.vn.caro_game.dtos.request.UpdateProfileRequest;
import com.vn.caro_game.dtos.response.UserProfileResponse;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.StatusCode;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.mappers.UserMapper;
import com.vn.caro_game.repositories.UserRepository;
import com.vn.caro_game.services.interfaces.IFileUploadService;
import com.vn.caro_game.services.interfaces.IUserProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of user profile service operations.
 *
 * <p>This service handles all user profile-related business logic including
 * profile updates, avatar management, and profile retrieval. It follows the
 * Single Responsibility Principle and implements proper error handling.</p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>Profile information retrieval</li>
 *   <li>Profile information updates</li>
 *   <li>Avatar upload and management</li>
 *   <li>Username uniqueness validation</li>
 *   <li>Transactional data consistency</li>
 * </ul>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileServiceImpl implements IUserProfileService {

    UserRepository userRepository;
    UserMapper userMapper;
    IFileUploadService fileUploadService;

    /**
     * Retrieves user profile information by user ID.
     *
     * @param userId the ID of the user to retrieve
     * @return UserProfileResponse containing user profile information
     * @throws CustomException if user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        log.info("Retrieving user profile for user ID: {}", userId);
        
        User user = findUserById(userId);
        UserProfileResponse response = userMapper.toProfileResponse(user);
        
        log.info("Successfully retrieved profile for user: {}", user.getUsername());
        return response;
    }

    /**
     * Updates user profile information.
     *
     * @param userId the ID of the user to update
     * @param request the update request containing new profile information
     * @return UserProfileResponse containing updated profile information
     * @throws CustomException if user is not found or username already exists
     */
    @Override
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating profile for user ID: {} with username: {}", userId, request.getUsername());
        
        User user = findUserById(userId);
        
        // Validate username uniqueness if changed
        validateUsernameChange(user, request.getUsername());
        
        // Update user with new information
        updateUserProfile(user, request);
        
        // Save updated user
        User savedUser = userRepository.save(user);
        UserProfileResponse response = userMapper.toProfileResponse(savedUser);
        
        log.info("Successfully updated profile for user: {}", savedUser.getUsername());
        return response;
    }

    /**
     * Updates user avatar.
     *
     * @param userId the ID of the user to update avatar for
     * @param avatarFile the avatar file to upload
     * @return UserProfileResponse containing updated profile with new avatar URL
     * @throws CustomException if user is not found or file upload fails
     */
    @Override
    @Transactional
    public UserProfileResponse updateAvatar(Long userId, MultipartFile avatarFile) {
        log.info("Updating avatar for user ID: {}", userId);
        
        User user = findUserById(userId);
        
        // Handle avatar update
        updateUserAvatar(user, avatarFile);
        
        // Save updated user
        User savedUser = userRepository.save(user);
        UserProfileResponse response = userMapper.toProfileResponse(savedUser);
        
        log.info("Successfully updated avatar for user: {}", savedUser.getUsername());
        return response;
    }

    /**
     * Updates both profile information and avatar.
     *
     * @param userId the ID of the user to update
     * @param request the update request containing new profile information
     * @param avatarFile the avatar file to upload (optional)
     * @return UserProfileResponse containing updated profile information
     * @throws CustomException if user is not found, username already exists, or file upload fails
     */
    @Override
    @Transactional
    public UserProfileResponse updateProfileWithAvatar(Long userId, UpdateProfileRequest request, MultipartFile avatarFile) {
        log.info("Updating complete profile for user ID: {} with username: {}", userId, request.getUsername());
        
        User user = findUserById(userId);
        
        // Validate username uniqueness if changed
        validateUsernameChange(user, request.getUsername());
        
        // Update profile information
        updateUserProfile(user, request);
        
        // Update avatar if provided
        if (isAvatarFileProvided(avatarFile)) {
            updateUserAvatar(user, avatarFile);
        }
        
        // Save updated user
        User savedUser = userRepository.save(user);
        UserProfileResponse response = userMapper.toProfileResponse(savedUser);
        
        log.info("Successfully updated complete profile for user: {}", savedUser.getUsername());
        return response;
    }

    /**
     * Finds user by ID or throws exception if not found.
     *
     * @param userId the ID of the user to find
     * @return User entity
     * @throws CustomException if user is not found
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new CustomException(UserProfileConstants.USER_NOT_FOUND, StatusCode.USER_NOT_FOUND);
                });
    }

    /**
     * Validates username change for uniqueness.
     *
     * @param user the current user
     * @param newUsername the new username to validate
     * @throws CustomException if username already exists
     */
    private void validateUsernameChange(User user, String newUsername) {
        if (!user.getUsername().equals(newUsername)) {
            validateUsernameUniqueness(newUsername, user.getId());
        }
    }

    /**
     * Updates user profile information from request.
     *
     * @param user the user entity to update
     * @param request the update request containing new information
     */
    private void updateUserProfile(User user, UpdateProfileRequest request) {
        userMapper.updateUserFromRequest(user, request);
    }

    /**
     * Updates user avatar and handles old avatar deletion.
     *
     * @param user the user entity to update
     * @param avatarFile the new avatar file
     * @throws CustomException if file upload fails
     */
    private void updateUserAvatar(User user, MultipartFile avatarFile) {
        try {
            // Delete old avatar if exists
            deleteOldAvatar(user);
            
            // Upload new avatar
            String newAvatarUrl = fileUploadService.uploadAvatar(avatarFile, user.getId());
            user.setAvatarUrl(newAvatarUrl);
            
        } catch (Exception e) {
            log.error("Failed to update avatar for user ID: {}", user.getId(), e);
            throw new CustomException(UserProfileConstants.AVATAR_UPDATE_FAILED, StatusCode.FILE_UPLOAD_ERROR);
        }
    }

    /**
     * Deletes old avatar file if it exists.
     *
     * @param user the user whose old avatar to delete
     */
    private void deleteOldAvatar(User user) {
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                fileUploadService.deleteAvatar(user.getAvatarUrl());
            } catch (Exception e) {
                log.warn("Failed to delete old avatar for user ID: {}, continuing with upload", user.getId(), e);
            }
        }
    }

    /**
     * Checks if avatar file is provided and not empty.
     *
     * @param avatarFile the avatar file to check
     * @return true if file is provided and not empty
     */
    private boolean isAvatarFileProvided(MultipartFile avatarFile) {
        return avatarFile != null && !avatarFile.isEmpty();
    }

    /**
     * Validates that the username is unique (excluding current user).
     *
     * @param username the username to validate
     * @param currentUserId the ID of the current user (to exclude from check)
     * @throws CustomException if username already exists
     */
    private void validateUsernameUniqueness(String username, Long currentUserId) {
        boolean usernameExists = userRepository.existsByUsernameAndIdNot(username, currentUserId);
        if (usernameExists) {
            log.error("Username already exists: {}", username);
            throw new CustomException(UserProfileConstants.USERNAME_ALREADY_EXISTS, StatusCode.USERNAME_ALREADY_EXISTS);
        }
    }
}
