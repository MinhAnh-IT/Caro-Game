package com.vn.caro_game.utils;

import com.vn.caro_game.exceptions.FileUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for handling file upload operations.
 *
 * <p>This utility provides methods for uploading, validating, and managing
 * user avatar files. It follows clean code principles with clear method names
 * and single responsibility.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@Component
@Slf4j
public class FileUploadUtil {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${app.upload.avatar-dir:uploads/avatars}")
    private String avatarUploadDir;

    /**
     * Uploads avatar file for a user.
     *
     * @param userId the ID of the user
     * @param file the avatar file to upload
     * @return relative URL path to the uploaded file
     * @throws FileUploadException if upload fails or file is invalid
     */
    public String uploadAvatarFile(Long userId, MultipartFile file) {
        validateFile(file);

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(avatarUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = String.format("user_%d_avatar_%s%s",
                userId, UUID.randomUUID(), extension);

            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            log.info("Avatar uploaded successfully for user {}: {}", userId, uniqueFilename);

            // Return relative URL
            return "/" + avatarUploadDir + "/" + uniqueFilename;

        } catch (IOException e) {
            log.error("Failed to upload avatar for user {}: {}", userId, e.getMessage());
            throw new FileUploadException("Failed to upload avatar file", e);
        }
    }

    /**
     * Deletes avatar file from filesystem.
     *
     * @param avatarUrl the URL of the avatar to delete
     */
    public void deleteAvatarFile(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return;
        }

        try {
            // Extract filename from URL
            String filename = avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(avatarUploadDir, filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Avatar file deleted: {}", filename);
            }
        } catch (IOException e) {
            log.error("Failed to delete avatar file {}: {}", avatarUrl, e.getMessage());
            // Don't throw exception for delete operations to avoid cascading failures
        }
    }

    /**
     * Validates uploaded file for avatar requirements.
     *
     * @param file the file to validate
     * @throws FileUploadException if file is invalid
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File size exceeds maximum limit of 5MB");
        }

        String contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new FileUploadException("Invalid file type. Only JPEG, PNG, and GIF are allowed");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !hasValidExtension(filename)) {
            throw new FileUploadException("Invalid file extension. Only .jpg, .jpeg, .png, .gif are allowed");
        }
    }

    /**
     * Checks if filename has valid extension.
     *
     * @param filename the filename to check
     * @return true if extension is valid, false otherwise
     */
    private boolean hasValidExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * Extracts file extension from filename.
     *
     * @param filename the filename
     * @return file extension including the dot
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex);
    }
}
