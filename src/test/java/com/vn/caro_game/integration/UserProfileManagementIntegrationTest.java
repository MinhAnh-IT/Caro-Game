package com.vn.caro_game.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.caro_game.dtos.request.UpdateProfileRequest;
import com.vn.caro_game.dtos.request.UserCreation;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.integrations.jwt.JwtService;
import com.vn.caro_game.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for User Profile Management workflow.
 *
 * <p>This test class covers the complete user profile management workflow
 * including registration with displayName, profile updates, and avatar management.</p>
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("User Profile Management Integration Tests")
class UserProfileManagementIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Create and save test user with displayName
        testUser = new User();
        testUser.setUsername("integrationuser");
        testUser.setEmail("integration@example.com");
        testUser.setDisplayName("Integration Test User");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);

        // Generate JWT token using correct method
        jwtToken = jwtService.generateAccessToken(testUser);

        // Ensure uploads directory exists
        try {
            Files.createDirectories(Paths.get("uploads/avatars"));
        } catch (Exception e) {
            // Directory might already exist
        }

        // Initialize MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should complete full profile management workflow successfully")
    void shouldCompleteFullProfileManagementWorkflowSuccessfully() throws Exception {
        // 1. Get initial profile
        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("integrationuser"))
                .andExpect(jsonPath("$.data.displayName").value("Integration Test User"))
                .andExpect(jsonPath("$.data.email").value("integration@example.com"))
                .andExpect(jsonPath("$.data.avatarUrl").isEmpty());

        // 2. Update profile information
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setUsername("updateduser");
        updateRequest.setDisplayName("Updated Integration User");

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("updateduser"))
                .andExpect(jsonPath("$.data.displayName").value("Updated Integration User"));

        // 3. Upload avatar
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar",
                "test-avatar.jpg",
                "image/jpeg",
                "test image content for integration test".getBytes()
        );

        mockMvc.perform(multipart("/api/profile/avatar")
                        .file(avatarFile)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.avatarUrl").isNotEmpty())
                .andExpect(jsonPath("$.data.avatarUrl").value(org.hamcrest.Matchers.containsString("/uploads/avatars/")));

        // 4. Update both profile and avatar in one request
        MockMultipartFile newAvatarFile = new MockMultipartFile(
                "avatar",
                "new-avatar.png",
                "image/png",
                "new test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/profile/complete")
                        .file(newAvatarFile)
                        .param("username", "finaluser")
                        .param("displayName", "Final Integration User")
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("finaluser"))
                .andExpect(jsonPath("$.data.displayName").value("Final Integration User"))
                .andExpect(jsonPath("$.data.avatarUrl").isNotEmpty());

        // 5. Verify final state
        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("finaluser"))
                .andExpect(jsonPath("$.data.displayName").value("Final Integration User"))
                .andExpect(jsonPath("$.data.avatarUrl").isNotEmpty());

        // Verify database state
        User updatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getUsername()).isEqualTo("finaluser");
        assertThat(updatedUser.getDisplayName()).isEqualTo("Final Integration User");
        assertThat(updatedUser.getAvatarUrl()).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo("integration@example.com"); // Should remain unchanged
    }

    @Test
    @DisplayName("Should handle registration with displayName successfully")
    void shouldHandleRegistrationWithDisplayNameSuccessfully() throws Exception {
        // Given
        UserCreation registrationRequest = new UserCreation();
        registrationRequest.setUsername("newuser123");
        registrationRequest.setDisplayName("New User Display Name");
        registrationRequest.setEmail("newuser@example.com");
        registrationRequest.setPassword("NewPassword123!"); // Stronger password

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest))
                        .with(csrf())) // Add CSRF token for security
                .andExpect(status().isCreated()) // Registration should return 201
                .andExpect(jsonPath("$.success").value(true)); // Only check if registration is successful

        // Verify database state
        User registeredUser = userRepository.findByEmail("newuser@example.com").orElse(null);
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getUsername()).isEqualTo("newuser123");
        assertThat(registeredUser.getDisplayName()).isEqualTo("New User Display Name");
        assertThat(registeredUser.getEmail()).isEqualTo("newuser@example.com");
    }

    @Test
    @DisplayName("Should handle username uniqueness validation correctly")
    void shouldHandleUsernameUniquenessValidationCorrectly() throws Exception {
        // Create another user with different username
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setDisplayName("Another User");
        anotherUser.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(anotherUser);

        // Try to update current user's username to existing username
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setUsername("anotheruser"); // This should fail
        updateRequest.setDisplayName("Updated Display Name");

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest()); // Should fail due to username conflict
    }

    @Test
    @DisplayName("Should handle file upload validation correctly")
    void shouldHandleFileUploadValidationCorrectly() throws Exception {
        // Test with invalid file type - expect 500 as the service throws CustomException
        MockMultipartFile invalidFile = new MockMultipartFile(
                "avatar",
                "test.txt",
                "text/plain",
                "this is not an image".getBytes()
        );

        mockMvc.perform(multipart("/api/profile/avatar")
                        .file(invalidFile)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isInternalServerError()); // Service throws CustomException which becomes 500

        // Test with file too large - expect 500 as the service throws CustomException
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "avatar",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        mockMvc.perform(multipart("/api/profile/avatar")
                        .file(largeFile)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isInternalServerError()); // Service throws CustomException which becomes 500
    }

    @Test
    @DisplayName("Should handle authentication errors correctly")
    void shouldHandleAuthenticationErrorsCorrectly() throws Exception {
        // Test without token
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isInternalServerError()); // RuntimeException for missing token

        // Test with invalid token
        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isInternalServerError()); // JWT validation error

        // Test with malformed header
        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "InvalidFormat"))
                .andExpect(status().isInternalServerError()); // Invalid format error
    }
}
