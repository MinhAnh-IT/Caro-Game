package com.vn.caro_game.services;

import com.vn.caro_game.dtos.request.UpdateProfileRequest;
import com.vn.caro_game.dtos.response.UserProfileResponse;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.StatusCode;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.mappers.UserMapper;
import com.vn.caro_game.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for managing user profile operations.
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
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileUploadService fileUploadService;

    /**
     * Retrieves user profile information by user ID.
     *
     * @param userId the ID of the user to retrieve
     * @return UserProfileResponse containing user profile information
     * @throws CustomException if user is not found
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", StatusCode.USER_NOT_FOUND));

        return userMapper.toProfileResponse(user);
    }

    /**
     * Updates user profile information.
     *
     * @param userId the ID of the user to update
     * @param request the update request containing new profile information
     * @return UserProfileResponse containing updated profile information
     * @throws CustomException if user is not found or username already exists
     */
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", StatusCode.USER_NOT_FOUND));

        // Check if username is being changed and if new username already exists
        if (!user.getUsername().equals(request.getUsername())) {
            validateUsernameUniqueness(request.getUsername(), userId);
        }

        // Update user with new information
        userMapper.updateUserFromRequest(user, request);

        // Save updated user
        User savedUser = userRepository.save(user);

        return userMapper.toProfileResponse(savedUser);
    }

    /**
     * Updates user avatar.
     *
     * @param userId the ID of the user to update avatar for
     * @param avatarFile the avatar file to upload
     * @return UserProfileResponse containing updated profile with new avatar URL
     * @throws CustomException if user is not found or file upload fails
     */
    @Transactional
    public UserProfileResponse updateAvatar(Long userId, MultipartFile avatarFile) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", StatusCode.USER_NOT_FOUND));

        // Delete old avatar if exists
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            fileUploadService.deleteAvatar(user.getAvatarUrl());
        }

        // Upload new avatar
        String newAvatarUrl = fileUploadService.uploadAvatar(avatarFile, userId);

        // Update user with new avatar URL
        user.setAvatarUrl(newAvatarUrl);
        User savedUser = userRepository.save(user);

        return userMapper.toProfileResponse(savedUser);
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
    @Transactional
    public UserProfileResponse updateProfileWithAvatar(Long userId, UpdateProfileRequest request, MultipartFile avatarFile) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", StatusCode.USER_NOT_FOUND));

        // Check if username is being changed and if new username already exists
        if (!user.getUsername().equals(request.getUsername())) {
            validateUsernameUniqueness(request.getUsername(), userId);
        }

        // Update profile information
        userMapper.updateUserFromRequest(user, request);

        // Update avatar if provided
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Delete old avatar if exists
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                fileUploadService.deleteAvatar(user.getAvatarUrl());
            }

            // Upload new avatar
            String newAvatarUrl = fileUploadService.uploadAvatar(avatarFile, userId);
            user.setAvatarUrl(newAvatarUrl);
        }

        // Save updated user
        User savedUser = userRepository.save(user);

        return userMapper.toProfileResponse(savedUser);
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
            throw new CustomException("Username already exists", StatusCode.USERNAME_ALREADY_EXISTS);
        }
    }
}
