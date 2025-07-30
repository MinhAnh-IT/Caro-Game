package com.vn.caro_game.services;

import com.vn.caro_game.enums.StatusCode;
import com.vn.caro_game.exceptions.CustomException;
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
 * Service for handling file upload operations.
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
public class FileUploadService {

    @Value("${app.upload.avatar.path:uploads/avatars}")
    private String avatarUploadPath;

    @Value("${app.upload.avatar.max-size:5242880}") // 5MB default
    private long maxAvatarSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
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
    public String uploadAvatar(MultipartFile file, Long userId) {
        validateAvatarFile(file);

        try {
            // Create upload directory if not exists
            Path uploadDir = Paths.get(avatarUploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("Created upload directory: {}", uploadDir);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String fileName = String.format("user_%d_avatar_%s.%s",
                    userId, UUID.randomUUID().toString(), extension);

            // Save file
            Path targetPath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Successfully uploaded avatar for user {}: {}", userId, fileName);

            // Return relative path for URL
            return String.format("/%s/%s", avatarUploadPath, fileName);

        } catch (IOException e) {
            log.error("Failed to upload avatar for user {}: {}", userId, e.getMessage(), e);
            throw new CustomException("Failed to upload avatar file", StatusCode.FILE_UPLOAD_ERROR);
        }
    }

    /**
     * Deletes an avatar file if it exists.
     *
     * @param avatarUrl the URL of the avatar to delete
     */
    public void deleteAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return;
        }

        try {
            // Extract filename from URL
            String fileName = Paths.get(avatarUrl).getFileName().toString();
            Path filePath = Paths.get(avatarUploadPath, fileName);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted avatar file: {}", fileName);
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
    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException("Avatar file is required", StatusCode.INVALID_REQUEST);
        }

        // Check file size
        if (file.getSize() > maxAvatarSize) {
            throw new CustomException(
                    String.format("Avatar file size cannot exceed %d MB", maxAvatarSize / 1024 / 1024),
                    StatusCode.FILE_TOO_LARGE);
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomException(
                    "Invalid file type. Only JPEG, PNG, GIF and WebP images are allowed",
                    StatusCode.INVALID_FILE_TYPE);
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new CustomException("Invalid filename", StatusCode.INVALID_FILE_TYPE);
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new CustomException(
                    "Invalid file extension. Only jpg, jpeg, png, gif, webp are allowed",
                    StatusCode.INVALID_FILE_TYPE);
        }
    }

    /**
     * Extracts file extension from filename.
     *
     * @param filename the filename to extract extension from
     * @return the file extension without dot
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
