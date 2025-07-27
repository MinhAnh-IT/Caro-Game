package com.vn.caro_game.services;

import com.vn.caro_game.exceptions.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
 * Unit tests for FileUploadService.
 *
 * <p>This test class covers file upload functionality, validation, and error scenarios.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileUploadService Tests")
class FileUploadServiceTest {

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private FileUploadService fileUploadService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Set up test configuration using reflection
        ReflectionTestUtils.setField(fileUploadService, "avatarUploadPath", tempDir.toString());
        ReflectionTestUtils.setField(fileUploadService, "maxAvatarSize", 5242880L); // 5MB
    }

    @Nested
    @DisplayName("Upload Avatar Tests")
    class UploadAvatarTests {

        @Test
        @DisplayName("Should upload avatar successfully with valid file")
        void shouldUploadAvatarSuccessfullyWithValidFile() throws IOException {
            // Given
            String filename = "test-avatar.jpg";
            String content = "test image content";
            Long userId = 1L;

            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getSize()).thenReturn(1024L); // 1KB
            when(mockFile.getContentType()).thenReturn("image/jpeg");
            when(mockFile.getOriginalFilename()).thenReturn(filename);
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes()));

            // When
            String result = fileUploadService.uploadAvatar(mockFile, userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("user_1_avatar_");
            assertThat(result).endsWith(".jpg");

            // Verify file was created
            assertThat(Files.list(tempDir)).hasSize(1);
        }

        @Test
        @DisplayName("Should create upload directory if not exists")
        void shouldCreateUploadDirectoryIfNotExists() throws IOException {
            // Given
            Path nonExistentDir = tempDir.resolve("new-dir");
            ReflectionTestUtils.setField(fileUploadService, "avatarUploadPath", nonExistentDir.toString());

            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getSize()).thenReturn(1024L);
            when(mockFile.getContentType()).thenReturn("image/png");
            when(mockFile.getOriginalFilename()).thenReturn("test.png");
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

            // When
            String result = fileUploadService.uploadAvatar(mockFile, 1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(Files.exists(nonExistentDir)).isTrue();
            assertThat(Files.list(nonExistentDir)).hasSize(1);
        }

        @Test
        @DisplayName("Should handle different image formats")
        void shouldHandleDifferentImageFormats() throws IOException {
            // Test PNG
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getSize()).thenReturn(1024L);
            when(mockFile.getContentType()).thenReturn("image/png");
            when(mockFile.getOriginalFilename()).thenReturn("test.png");
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

            String pngResult = fileUploadService.uploadAvatar(mockFile, 1L);
            assertThat(pngResult).endsWith(".png");

            // Test GIF
            when(mockFile.getContentType()).thenReturn("image/gif");
            when(mockFile.getOriginalFilename()).thenReturn("test.gif");
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

            String gifResult = fileUploadService.uploadAvatar(mockFile, 2L);
            assertThat(gifResult).endsWith(".gif");
        }
    }

    @Nested
    @DisplayName("File Validation Tests")
    class FileValidationTests {

        @Test
        @DisplayName("Should throw exception when file is null")
        void shouldThrowExceptionWhenFileIsNull() {
            // When & Then
            assertThatThrownBy(() -> fileUploadService.uploadAvatar(null, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Avatar file is required");
        }

        @Test
        @DisplayName("Should throw exception when file is empty")
        void shouldThrowExceptionWhenFileIsEmpty() {
            // Given
            when(mockFile.isEmpty()).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> fileUploadService.uploadAvatar(mockFile, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Avatar file is required");
        }

        @Test
        @DisplayName("Should throw exception when file size exceeds limit")
        void shouldThrowExceptionWhenFileSizeExceedsLimit() {
            // Given
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getSize()).thenReturn(10485760L); // 10MB

            // When & Then
            assertThatThrownBy(() -> fileUploadService.uploadAvatar(mockFile, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Avatar file size cannot exceed");
        }

        @Test
        @DisplayName("Should throw exception for invalid content type")
        void shouldThrowExceptionForInvalidContentType() {
            // Given
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getSize()).thenReturn(1024L);
            when(mockFile.getContentType()).thenReturn("text/plain");

            // When & Then
            assertThatThrownBy(() -> fileUploadService.uploadAvatar(mockFile, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Invalid file type");
        }

        @Test
        @DisplayName("Should throw exception for null content type")
        void shouldThrowExceptionForNullContentType() {
            // Given
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getSize()).thenReturn(1024L);
            when(mockFile.getContentType()).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> fileUploadService.uploadAvatar(mockFile, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Invalid file type");
        }

        @Test
        @DisplayName("Should throw exception for invalid file extension")
        void shouldThrowExceptionForInvalidFileExtension() {
            // Given
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getSize()).thenReturn(1024L);
            when(mockFile.getContentType()).thenReturn("image/jpeg");
            when(mockFile.getOriginalFilename()).thenReturn("test.txt");

            // When & Then
            assertThatThrownBy(() -> fileUploadService.uploadAvatar(mockFile, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Invalid file extension");
        }

        @Test
        @DisplayName("Should throw exception for null filename")
        void shouldThrowExceptionForNullFilename() {
            // Given
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getSize()).thenReturn(1024L);
            when(mockFile.getContentType()).thenReturn("image/jpeg");
            when(mockFile.getOriginalFilename()).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> fileUploadService.uploadAvatar(mockFile, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Invalid filename");
        }

        @Test
        @DisplayName("Should throw exception for filename without extension")
        void shouldThrowExceptionForFilenameWithoutExtension() {
            // Given
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getSize()).thenReturn(1024L);
            when(mockFile.getContentType()).thenReturn("image/jpeg");
            when(mockFile.getOriginalFilename()).thenReturn("test-file-no-extension");

            // When & Then
            assertThatThrownBy(() -> fileUploadService.uploadAvatar(mockFile, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Invalid file extension");
        }
    }

    @Nested
    @DisplayName("Delete Avatar Tests")
    class DeleteAvatarTests {

        @Test
        @DisplayName("Should delete avatar file when exists")
        void shouldDeleteAvatarFileWhenExists() throws IOException {
            // Given
            Path testFile = tempDir.resolve("test-avatar.jpg");
            Files.createFile(testFile);
            String avatarUrl = "/" + tempDir.getFileName() + "/test-avatar.jpg";

            assertThat(Files.exists(testFile)).isTrue();

            // When
            fileUploadService.deleteAvatar(avatarUrl);

            // Then
            assertThat(Files.exists(testFile)).isFalse();
        }

        @Test
        @DisplayName("Should handle gracefully when file does not exist")
        void shouldHandleGracefullyWhenFileDoesNotExist() {
            // Given
            String avatarUrl = "/" + tempDir.getFileName() + "/non-existent.jpg";

            // When & Then - Should not throw exception
            assertThatCode(() -> fileUploadService.deleteAvatar(avatarUrl))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle null avatar URL gracefully")
        void shouldHandleNullAvatarUrlGracefully() {
            // When & Then - Should not throw exception
            assertThatCode(() -> fileUploadService.deleteAvatar(null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle empty avatar URL gracefully")
        void shouldHandleEmptyAvatarUrlGracefully() {
            // When & Then - Should not throw exception
            assertThatCode(() -> fileUploadService.deleteAvatar(""))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw CustomException when IOException occurs during upload")
        void shouldThrowCustomExceptionWhenIOExceptionOccursDuringUpload() throws IOException {
            // Given
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getSize()).thenReturn(1024L);
            when(mockFile.getContentType()).thenReturn("image/jpeg");
            when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
            when(mockFile.getInputStream()).thenThrow(new IOException("Simulated IO error"));

            // When & Then
            assertThatThrownBy(() -> fileUploadService.uploadAvatar(mockFile, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Failed to upload avatar file");
        }
    }
}
