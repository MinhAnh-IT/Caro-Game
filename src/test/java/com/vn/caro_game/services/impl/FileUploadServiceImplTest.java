package com.vn.caro_game.services.impl;

import com.vn.caro_game.constants.UserProfileConstants;
import com.vn.caro_game.exceptions.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FileUploadServiceImpl.
 *
 * <p>This test class covers file upload functionality, validation, and error scenarios
 * for the implementation service following clean architecture principles.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileUploadServiceImpl Tests")
class FileUploadServiceImplTest {

    private FileUploadServiceImpl fileUploadService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileUploadService = new FileUploadServiceImpl();
        
        // Set up test configuration using reflection
        ReflectionTestUtils.setField(fileUploadService, "avatarUploadPath", tempDir.toString());
        ReflectionTestUtils.setField(fileUploadService, "maxAvatarSize", 5 * 1024 * 1024L); // 5MB
    }

    @Nested
    @DisplayName("Upload Avatar Tests")
    class UploadAvatarTests {

        @Test
        @DisplayName("Should upload avatar successfully")
        void shouldUploadAvatarSuccessfully() throws IOException {
            // Given
            MultipartFile mockFile = createValidMockFile("test.jpg", "image/jpeg", 1024);
            Long userId = 1L;

            // When
            String result = fileUploadService.uploadAvatar(mockFile, userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).startsWith("/" + tempDir.toString() + "/");
            assertThat(result).contains("user_1_avatar_");
            assertThat(result).endsWith(".jpg");
            
            // Verify file was actually created
            String fileName = result.substring(result.lastIndexOf("/") + 1);
            Path uploadedFile = tempDir.resolve(fileName);
            assertThat(Files.exists(uploadedFile)).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when file is null")
        void shouldThrowExceptionWhenFileIsNull() {
            // When & Then
            assertThatThrownBy(() -> fileUploadService.uploadAvatar(null, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UserProfileConstants.INVALID_FILE_FORMAT);
        }

        @Test
        @DisplayName("Should throw exception when file is empty")
        void shouldThrowExceptionWhenFileIsEmpty() {
            // Given
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.isEmpty()).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> fileUploadService.uploadAvatar(mockFile, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UserProfileConstants.INVALID_FILE_FORMAT);
        }

        @Test
        @DisplayName("Should throw exception when file size exceeds limit")
        void shouldThrowExceptionWhenFileSizeExceedsLimit() {
            // Given
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getSize()).thenReturn(6 * 1024 * 1024L); // 6MB

            // When & Then
            assertThatThrownBy(() -> fileUploadService.uploadAvatar(mockFile, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UserProfileConstants.FILE_SIZE_EXCEEDED);
        }

        @Test
        @DisplayName("Should throw exception when content type is invalid")
        void shouldThrowExceptionWhenContentTypeIsInvalid() {
            // Given
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getSize()).thenReturn(1024L);
            when(mockFile.getContentType()).thenReturn("text/plain");

            // When & Then
            assertThatThrownBy(() -> fileUploadService.uploadAvatar(mockFile, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UserProfileConstants.INVALID_FILE_FORMAT);
        }

        @Test
        @DisplayName("Should throw exception when file extension is invalid")
        void shouldThrowExceptionWhenFileExtensionIsInvalid() {
            // Given
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.getContentType()).thenReturn("image/jpeg");
            when(mockFile.getOriginalFilename()).thenReturn("test.txt");

            // When & Then
            assertThatThrownBy(() -> fileUploadService.uploadAvatar(mockFile, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UserProfileConstants.INVALID_FILE_FORMAT);
        }

        @Test
        @DisplayName("Should handle different valid image types")
        void shouldHandleDifferentValidImageTypes() throws IOException {
            // Test cases for different valid formats
            String[][] testCases = {
                    {"test.png", "image/png"},
                    {"test.gif", "image/gif"},
                    {"test.webp", "image/webp"},
                    {"test.jpeg", "image/jpeg"}
            };

            for (String[] testCase : testCases) {
                // Given
                MultipartFile mockFile = createValidMockFile(testCase[0], testCase[1], 1024);
                Long userId = 1L;

                // When
                String result = fileUploadService.uploadAvatar(mockFile, userId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result).contains("user_1_avatar_");
            }
        }
    }

    @Nested
    @DisplayName("Delete Avatar Tests")
    class DeleteAvatarTests {

        @Test
        @DisplayName("Should delete existing avatar file")
        void shouldDeleteExistingAvatarFile() throws IOException {
            // Given
            String fileName = "user_1_avatar_test.jpg";
            Path avatarFile = tempDir.resolve(fileName);
            Files.createFile(avatarFile);
            
            String avatarUrl = "/" + tempDir.toString() + "/" + fileName;

            // When
            fileUploadService.deleteAvatar(avatarUrl);

            // Then
            assertThat(Files.exists(avatarFile)).isFalse();
        }

        @Test
        @DisplayName("Should handle deletion of non-existent file gracefully")
        void shouldHandleDeletionOfNonExistentFileGracefully() {
            // Given
            String avatarUrl = "/" + tempDir.toString() + "/non_existent_file.jpg";

            // When & Then (should not throw exception)
            assertThatCode(() -> fileUploadService.deleteAvatar(avatarUrl))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle null avatar URL gracefully")
        void shouldHandleNullAvatarUrlGracefully() {
            // When & Then (should not throw exception)
            assertThatCode(() -> fileUploadService.deleteAvatar(null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle empty avatar URL gracefully")
        void shouldHandleEmptyAvatarUrlGracefully() {
            // When & Then (should not throw exception)
            assertThatCode(() -> fileUploadService.deleteAvatar(""))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate file successfully for valid avatar")
        void shouldValidateFileSuccessfullyForValidAvatar() {
            // Given
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
            when(mockFile.getContentType()).thenReturn("image/jpeg");
            when(mockFile.getSize()).thenReturn(1024L);

            // When & Then (should not throw exception)
            assertThatCode(() -> fileUploadService.validateAvatarFile(mockFile))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject file with null filename")
        void shouldRejectFileWithNullFilename() {
            // Given
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getSize()).thenReturn(1024L);
            when(mockFile.getContentType()).thenReturn("image/jpeg");
            when(mockFile.getOriginalFilename()).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> fileUploadService.validateAvatarFile(mockFile))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UserProfileConstants.INVALID_FILE_FORMAT);
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("Should generate unique avatar filename")
        void shouldGenerateUniqueAvatarFilename() {
            // Given
            String originalFilename = "avatar.jpg";
            Long userId = 1L;

            // When
            String result1 = fileUploadService.generateAvatarFilename(originalFilename, userId);
            String result2 = fileUploadService.generateAvatarFilename(originalFilename, userId);

            // Then
            assertThat(result1).isNotEqualTo(result2);
            assertThat(result1).startsWith("user_1_avatar_");
            assertThat(result1).endsWith(".jpg");
            assertThat(result2).startsWith("user_1_avatar_");
            assertThat(result2).endsWith(".jpg");
        }

        @Test
        @DisplayName("Should create upload directory if not exists")
        void shouldCreateUploadDirectoryIfNotExists() {
            // Given
            Path newDir = tempDir.resolve("new_upload_dir");
            assertThat(Files.exists(newDir)).isFalse();

            // When
            fileUploadService.createUploadDirectoryIfNotExists(newDir.toString());

            // Then
            assertThat(Files.exists(newDir)).isTrue();
            assertThat(Files.isDirectory(newDir)).isTrue();
        }
    }

    // Helper methods

    private MultipartFile createValidMockFile(String filename, String contentType, long size) {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(size);
        when(mockFile.getContentType()).thenReturn(contentType);
        when(mockFile.getOriginalFilename()).thenReturn(filename);
        
        try {
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[(int) size]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return mockFile;
    }
}
