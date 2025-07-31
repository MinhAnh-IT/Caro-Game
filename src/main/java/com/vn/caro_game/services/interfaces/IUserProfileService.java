package com.vn.caro_game.services.interfaces;

import com.vn.caro_game.dtos.request.UpdateProfileRequest;
import com.vn.caro_game.dtos.response.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for user profile operations.
 *
 * <p>This interface defines the contract for user profile management operations
 * including retrieving, updating profile information and avatar management.</p>
 *
 * <p>Following SOLID principles:
 * - Single Responsibility: Focused only on user profile operations
 * - Open/Closed: Can be extended without modification
 * - Interface Segregation: Specific to user profile concerns
 * </p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
public interface IUserProfileService {

    /**
     * Retrieves user profile information by user ID.
     *
     * @param userId the ID of the user whose profile to retrieve
     * @return UserProfileResponse containing user profile data
     * @throws com.vn.caro_game.exceptions.CustomException if user not found
     */
    UserProfileResponse getUserProfile(Long userId);

    /**
     * Updates user profile information.
     *
     * @param userId the ID of the user whose profile to update
     * @param request the profile update request containing new information
     * @return UserProfileResponse containing updated profile data
     * @throws com.vn.caro_game.exceptions.CustomException if user not found or validation fails
     */
    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * Uploads and updates user avatar.
     *
     * @param userId the ID of the user whose avatar to update
     * @param avatarFile the avatar image file to upload
     * @return UserProfileResponse containing updated profile data with new avatar URL
     * @throws com.vn.caro_game.exceptions.CustomException if user not found or file upload fails
     */
    UserProfileResponse updateAvatar(Long userId, MultipartFile avatarFile);

    /**
     * Updates both profile information and avatar in a single operation.
     *
     * @param userId the ID of the user whose profile to update
     * @param request the profile update request containing new information
     * @param avatarFile the optional avatar image file to upload
     * @return UserProfileResponse containing updated profile data
     * @throws com.vn.caro_game.exceptions.CustomException if user not found, validation fails, or file upload fails
     */
    UserProfileResponse updateProfileWithAvatar(Long userId, UpdateProfileRequest request, MultipartFile avatarFile);
}
