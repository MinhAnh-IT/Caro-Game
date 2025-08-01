package com.vn.caro_game.services.impl;

import com.vn.caro_game.constants.UserProfileConstants;
import com.vn.caro_game.enums.StatusCode;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.services.interfaces.IFileUploadService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of file upload service operations.
 *
 * <p>This service provides file upload functionality with validation and storage
 * management. It follows security best practices for file handling and ensures
 * proper file type validation.</p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>File type validation for images</li>
 *   <li>File size validation</li>
 *   <li>Secure file naming with UUID</li>
 *   <li>Directory creation if not exists</li>
 *   <li>Old file cleanup</li>
 * </ul>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileUploadServiceImpl implements IFileUploadService {

    @Value("${app.upload.avatar.path:uploads/avatars}")
    String avatarUploadPath;

    @Value("${app.upload.avatar.max-size:5242880}") // 5MB default
    long maxAvatarSize;

    static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    /**
     * Uploads an avatar file for a user.
     *
     * @param file the multipart file to upload
     * @param userId the ID of the user uploading the avatar
     * @return the relative path to the uploaded file
     * @throws CustomException if upload fails or file is invalid
     */
    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        log.info("Starting avatar upload for user ID: {}", userId);
        
        validateAvatarFile(file);

        try {
            // Create upload directory if not exists
            createUploadDirectoryIfNotExists(avatarUploadPath);

            // Generate unique filename
            String fileName = generateAvatarFilename(file.getOriginalFilename(), userId);

            // Save file
            Path targetPath = Paths.get(avatarUploadPath).resolve(fileName);
            saveFileToPath(file, targetPath);

            String avatarUrl = buildAvatarUrl(fileName);
            log.info("Successfully uploaded avatar for user {}: {}", userId, fileName);

            return avatarUrl;

        } catch (IOException e) {
            log.error("Failed to upload avatar for user {}: {}", userId, e.getMessage(), e);
            throw new CustomException(UserProfileConstants.FILE_UPLOAD_FAILED, StatusCode.FILE_UPLOAD_ERROR);
        }
    }

    /**
     * Deletes an avatar file if it exists.
     *
     * @param avatarUrl the URL of the avatar to delete
     */
    @Override
    public void deleteAvatar(String avatarUrl) {
        if (isAvatarUrlEmpty(avatarUrl)) {
            return;
        }

        try {
            Path filePath = extractFilePathFromUrl(avatarUrl);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted avatar file: {}", filePath.getFileName());
            }
        } catch (IOException e) {
            log.warn("Failed to delete avatar file {}: {}", avatarUrl, e.getMessage());
            // Don't throw exception for delete failures as it's not critical
        }
    }

    /**
     * Validates avatar file before upload.
     *
     * @param file the file to validate
     * @throws CustomException if file is invalid
     */
    @Override
    public void validateAvatarFile(MultipartFile file) {
        validateFileNotEmpty(file);
        validateFileSize(file);
        validateFileContentType(file);
        validateFileExtension(file);
    }

    /**
     * Generates a unique filename for the avatar.
     *
     * @param originalFilename the original filename of the uploaded file
     * @param userId the ID of the user for whom the avatar is being uploaded
     * @return the generated unique filename
     */
    @Override
    public String generateAvatarFilename(String originalFilename, Long userId) {
        String extension = extractFileExtension(originalFilename);
        return String.format("user_%d_avatar_%s.%s",
                userId, UUID.randomUUID().toString(), extension);
    }

    /**
     * Creates the upload directory if it doesn't exist.
     *
     * @param uploadPath the path where files should be uploaded
     * @throws CustomException if directory creation fails
     */
    @Override
    public void createUploadDirectoryIfNotExists(String uploadPath) {
        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("Created upload directory: {}", uploadDir);
            }
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", uploadPath, e);
            throw new CustomException(UserProfileConstants.FILE_UPLOAD_FAILED, StatusCode.FILE_UPLOAD_ERROR);
        }
    }

    /**
     * Validates that the file is not null or empty.
     *
     * @param file the file to validate
     * @throws CustomException if file is null or empty
     */
    private void validateFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(UserProfileConstants.INVALID_FILE_FORMAT, StatusCode.INVALID_REQUEST);
        }
    }

    /**
     * Validates file size against maximum allowed size.
     *
     * @param file the file to validate
     * @throws CustomException if file size exceeds limit
     */
    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > maxAvatarSize) {
            throw new CustomException(UserProfileConstants.FILE_SIZE_EXCEEDED, StatusCode.FILE_TOO_LARGE);
        }
    }

    /**
     * Validates file content type.
     *
     * @param file the file to validate
     * @throws CustomException if content type is not allowed
     */
    private void validateFileContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomException(UserProfileConstants.INVALID_FILE_FORMAT, StatusCode.INVALID_FILE_TYPE);
        }
    }

    /**
     * Validates file extension.
     *
     * @param file the file to validate
     * @throws CustomException if file extension is not allowed
     */
    private void validateFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new CustomException(UserProfileConstants.INVALID_FILE_FORMAT, StatusCode.INVALID_FILE_TYPE);
        }

        String extension = extractFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new CustomException(UserProfileConstants.INVALID_FILE_FORMAT, StatusCode.INVALID_FILE_TYPE);
        }
    }

    /**
     * Extracts file extension from filename.
     *
     * @param filename the filename to extract extension from
     * @return the file extension without dot
     */
    private String extractFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Saves the file to the specified path.
     *
     * @param file the file to save
     * @param targetPath the target path where file should be saved
     * @throws IOException if file saving fails
     */
    private void saveFileToPath(MultipartFile file, Path targetPath) throws IOException {
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Builds the avatar URL from filename.
     *
     * @param fileName the filename
     * @return the avatar URL
     */
    private String buildAvatarUrl(String fileName) {
        return String.format("/%s/%s", avatarUploadPath, fileName);
    }

    /**
     * Checks if avatar URL is null or empty.
     *
     * @param avatarUrl the avatar URL to check
     * @return true if URL is null or empty
     */
    private boolean isAvatarUrlEmpty(String avatarUrl) {
        return avatarUrl == null || avatarUrl.isEmpty();
    }

    /**
     * Extracts file path from avatar URL.
     *
     * @param avatarUrl the avatar URL
     * @return the file path
     */
    private Path extractFilePathFromUrl(String avatarUrl) {
        String fileName = Paths.get(avatarUrl).getFileName().toString();
        return Paths.get(avatarUploadPath, fileName);
    }
}
