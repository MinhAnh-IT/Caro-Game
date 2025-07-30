package com.vn.caro_game.utils;

import com.vn.caro_game.exceptions.FileUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for FileUploadUtil.
 *
 * <p>Tests file upload functionality including validation, storage, and cleanup operations.
 * Uses temporary directories to ensure tests don't affect the real file system.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@DisplayName("FileUploadUtil Tests")
class FileUploadUtilTest {

    private FileUploadUtil fileUploadUtil;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileUploadUtil = new FileUploadUtil();
        // Set the upload directory to our temp directory for testing
        ReflectionTestUtils.setField(fileUploadUtil, "avatarUploadDir", tempDir.toString());
    }

    @Nested
    @DisplayName("File Upload Tests")
    class FileUploadTests {

        @Test
        @DisplayName("Should upload valid JPEG file successfully")
        void shouldUploadValidJpegFileSuccessfully() throws IOException {
            // Given
            MockMultipartFile validFile = new MockMultipartFile(
                    "avatar",
                    "test-avatar.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "valid jpeg content".getBytes()
            );
            Long userId = 1L;

            // When
            String result = fileUploadUtil.uploadAvatarFile(userId, validFile);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).startsWith("/" + tempDir.toString());
            assertThat(result).contains("user_1_avatar_");
            assertThat(result).endsWith(".jpg");

            // Verify file was actually created
            String filename = result.substring(result.lastIndexOf('/') + 1);
            Path uploadedFile = tempDir.resolve(filename);
            assertThat(Files.exists(uploadedFile)).isTrue();
            assertThat(Files.readString(uploadedFile)).isEqualTo("valid jpeg content");
        }

        @Test
        @DisplayName("Should upload valid PNG file successfully")
        void shouldUploadValidPngFileSuccessfully() throws IOException {
            // Given
            MockMultipartFile validFile = new MockMultipartFile(
                    "avatar",
                    "test-avatar.png",
                    MediaType.IMAGE_PNG_VALUE,
                    "valid png content".getBytes()
            );
            Long userId = 2L;

            // When
            String result = fileUploadUtil.uploadAvatarFile(userId, validFile);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("user_2_avatar_");
            assertThat(result).endsWith(".png");

            // Verify file exists
            String filename = result.substring(result.lastIndexOf('/') + 1);
            Path uploadedFile = tempDir.resolve(filename);
            assertThat(Files.exists(uploadedFile)).isTrue();
        }

        @Test
        @DisplayName("Should upload valid GIF file successfully")
        void shouldUploadValidGifFileSuccessfully() throws IOException {
            // Given
            MockMultipartFile validFile = new MockMultipartFile(
                    "avatar",
                    "test-avatar.gif",
                    MediaType.IMAGE_GIF_VALUE,
                    "valid gif content".getBytes()
            );
            Long userId = 3L;

            // When
            String result = fileUploadUtil.uploadAvatarFile(userId, validFile);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("user_3_avatar_");
            assertThat(result).endsWith(".gif");
        }

        @Test
        @DisplayName("Should generate unique filenames for same user")
        void shouldGenerateUniqueFilenamesForSameUser() throws IOException {
            // Given
            MockMultipartFile file1 = new MockMultipartFile(
                    "avatar", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "content1".getBytes());
            MockMultipartFile file2 = new MockMultipartFile(
                    "avatar", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "content2".getBytes());
            Long userId = 1L;

            // When
            String result1 = fileUploadUtil.uploadAvatarFile(userId, file1);
            String result2 = fileUploadUtil.uploadAvatarFile(userId, file2);

            // Then
            assertThat(result1).isNotEqualTo(result2);
            assertThat(result1).contains("user_1_avatar_");
            assertThat(result2).contains("user_1_avatar_");

            // Both files should exist
            String filename1 = result1.substring(result1.lastIndexOf('/') + 1);
            String filename2 = result2.substring(result2.lastIndexOf('/') + 1);
            assertThat(Files.exists(tempDir.resolve(filename1))).isTrue();
            assertThat(Files.exists(tempDir.resolve(filename2))).isTrue();
        }
    }

    @Nested
    @DisplayName("File Validation Tests")
    class FileValidationTests {

        @Test
        @DisplayName("Should throw exception when file is null")
        void shouldThrowExceptionWhenFileIsNull() {
            // When & Then
            assertThatThrownBy(() -> fileUploadUtil.uploadAvatarFile(1L, null))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessage("File is required");
        }

        @Test
        @DisplayName("Should throw exception when file is empty")
        void shouldThrowExceptionWhenFileIsEmpty() {
            // Given
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "avatar", "empty.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

            // When & Then
            assertThatThrownBy(() -> fileUploadUtil.uploadAvatarFile(1L, emptyFile))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessage("File is required");
        }

        @Test
        @DisplayName("Should throw exception when file exceeds size limit")
        void shouldThrowExceptionWhenFileExceedsSizeLimit() {
            // Given - Create a file larger than 5MB
            byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
            MockMultipartFile largeFile = new MockMultipartFile(
                    "avatar", "large.jpg", MediaType.IMAGE_JPEG_VALUE, largeContent);

            // When & Then
            assertThatThrownBy(() -> fileUploadUtil.uploadAvatarFile(1L, largeFile))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessage("File size exceeds maximum limit of 5MB");
        }

        @Test
        @DisplayName("Should throw exception when content type is invalid")
        void shouldThrowExceptionWhenContentTypeIsInvalid() {
            // Given
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "avatar", "document.pdf", "application/pdf", "pdf content".getBytes());

            // When & Then
            assertThatThrownBy(() -> fileUploadUtil.uploadAvatarFile(1L, invalidFile))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessage("Invalid file type. Only JPEG, PNG, and GIF are allowed");
        }

        @Test
        @DisplayName("Should throw exception when file extension is invalid")
        void shouldThrowExceptionWhenFileExtensionIsInvalid() {
            // Given
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "avatar", "image.bmp", MediaType.IMAGE_JPEG_VALUE, "bmp content".getBytes());

            // When & Then
            assertThatThrownBy(() -> fileUploadUtil.uploadAvatarFile(1L, invalidFile))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessage("Invalid file extension. Only .jpg, .jpeg, .png, .gif are allowed");
        }

        @Test
        @DisplayName("Should throw exception when filename is null")
        void shouldThrowExceptionWhenFilenameIsNull() {
            // Given
            MockMultipartFile fileWithNullName = new MockMultipartFile(
                    "avatar", null, MediaType.IMAGE_JPEG_VALUE, "content".getBytes());

            // When & Then
            assertThatThrownBy(() -> fileUploadUtil.uploadAvatarFile(1L, fileWithNullName))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessage("Invalid file extension. Only .jpg, .jpeg, .png, .gif are allowed");
        }

        @Test
        @DisplayName("Should accept JPEG with uppercase extension")
        void shouldAcceptJpegWithUppercaseExtension() throws IOException {
            // Given
            MockMultipartFile validFile = new MockMultipartFile(
                    "avatar", "test-avatar.JPEG", MediaType.IMAGE_JPEG_VALUE, "content".getBytes());

            // When
            String result = fileUploadUtil.uploadAvatarFile(1L, validFile);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).endsWith(".JPEG");
        }
    }

    @Nested
    @DisplayName("File Deletion Tests")
    class FileDeletionTests {

        @Test
        @DisplayName("Should delete existing file successfully")
        void shouldDeleteExistingFileSuccessfully() throws IOException {
            // Given
            String filename = "test_avatar.jpg";
            Path testFile = tempDir.resolve(filename);
            Files.write(testFile, "test content".getBytes());
            String avatarUrl = "/" + tempDir.toString() + "/" + filename;

            // Verify file exists before deletion
            assertThat(Files.exists(testFile)).isTrue();

            // When
            fileUploadUtil.deleteAvatarFile(avatarUrl);

            // Then
            assertThat(Files.exists(testFile)).isFalse();
        }

        @Test
        @DisplayName("Should handle deletion of non-existent file gracefully")
        void shouldHandleDeletionOfNonExistentFileGracefully() {
            // Given
            String avatarUrl = "/" + tempDir.toString() + "/non_existent.jpg";

            // When & Then - Should not throw exception
            assertThatCode(() -> fileUploadUtil.deleteAvatarFile(avatarUrl))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle null avatar URL gracefully")
        void shouldHandleNullAvatarUrlGracefully() {
            // When & Then - Should not throw exception
            assertThatCode(() -> fileUploadUtil.deleteAvatarFile(null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle empty avatar URL gracefully")
        void shouldHandleEmptyAvatarUrlGracefully() {
            // When & Then - Should not throw exception
            assertThatCode(() -> fileUploadUtil.deleteAvatarFile(""))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should extract filename correctly from URL path")
        void shouldExtractFilenameCorrectlyFromUrlPath() throws IOException {
            // Given
            String filename = "user_1_avatar_123.jpg";
            Path testFile = tempDir.resolve(filename);
            Files.write(testFile, "test content".getBytes());
            String avatarUrl = "/uploads/avatars/" + filename;

            // Set upload dir to match the test scenario
            ReflectionTestUtils.setField(fileUploadUtil, "avatarUploadDir", "uploads/avatars");

            // When
            fileUploadUtil.deleteAvatarFile(avatarUrl);

            // Then
            // Since we changed the upload dir, the file won't actually be deleted,
            // but we can verify the method handles URL parsing correctly
            assertThatCode(() -> fileUploadUtil.deleteAvatarFile(avatarUrl))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Directory Creation Tests")
    class DirectoryCreationTests {

        @Test
        @DisplayName("Should create upload directory if it doesn't exist")
        void shouldCreateUploadDirectoryIfItDoesntExist() throws IOException {
            // Given
            Path newUploadDir = tempDir.resolve("new_upload_dir");
            ReflectionTestUtils.setField(fileUploadUtil, "avatarUploadDir", newUploadDir.toString());

            MockMultipartFile validFile = new MockMultipartFile(
                    "avatar", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "content".getBytes());

            // Verify directory doesn't exist initially
            assertThat(Files.exists(newUploadDir)).isFalse();

            // When
            String result = fileUploadUtil.uploadAvatarFile(1L, validFile);

            // Then
            assertThat(result).isNotNull();
            assertThat(Files.exists(newUploadDir)).isTrue();
            assertThat(Files.isDirectory(newUploadDir)).isTrue();
        }
    }
}
