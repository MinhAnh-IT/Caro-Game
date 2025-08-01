package com.vn.caro_game.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for file upload operations.
 *
 * <p>This interface defines the contract for file upload management operations
 * including avatar upload, deletion, and validation.</p>
 *
 * <p>Following SOLID principles:
 * - Single Responsibility: Focused only on file upload operations
 * - Open/Closed: Can be extended without modification
 * - Interface Segregation: Specific to file upload concerns
 * </p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
public interface IFileUploadService {

    /**
     * Uploads an avatar file for the specified user.
     *
     * @param file the avatar file to upload
     * @param userId the ID of the user for whom the avatar is being uploaded
     * @return the URL path of the uploaded avatar file
     * @throws com.vn.caro_game.exceptions.CustomException if file upload fails or validation errors occur
     */
    String uploadAvatar(MultipartFile file, Long userId);

    /**
     * Deletes an avatar file from the storage.
     *
     * @param avatarUrl the URL path of the avatar file to delete
     * @throws com.vn.caro_game.exceptions.CustomException if file deletion fails
     */
    void deleteAvatar(String avatarUrl);

    /**
     * Validates if the provided file is a valid avatar file.
     *
     * @param file the file to validate
     * @throws com.vn.caro_game.exceptions.CustomException if validation fails
     */
    void validateAvatarFile(MultipartFile file);

    /**
     * Generates a unique filename for the avatar.
     *
     * @param originalFilename the original filename of the uploaded file
     * @param userId the ID of the user for whom the avatar is being uploaded
     * @return the generated unique filename
     */
    String generateAvatarFilename(String originalFilename, Long userId);

    /**
     * Creates the upload directory if it doesn't exist.
     *
     * @param uploadPath the path where files should be uploaded
     * @throws com.vn.caro_game.exceptions.CustomException if directory creation fails
     */
    void createUploadDirectoryIfNotExists(String uploadPath);
}
