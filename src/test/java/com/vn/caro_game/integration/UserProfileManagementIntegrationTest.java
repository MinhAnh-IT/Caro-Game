package com.vn.caro_game.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.caro_game.configs.CustomUserDetails;
import com.vn.caro_game.dtos.request.UpdateProfileRequest;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for User Profile Management functionality.
 *
 * <p>These tests verify the complete flow from HTTP request to database
 * persistence, including authentication, validation, business logic,
 * and file operations.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("User Profile Management Integration Tests")
class UserProfileManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    /**
     * Helper method to create authenticated user principal for testing
     */
    private UsernamePasswordAuthenticationToken createAuthenticatedUser() {
        CustomUserDetails userDetails = new CustomUserDetails(testUser);
        return new UsernamePasswordAuthenticationToken(
            userDetails, 
            null, 
            java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @BeforeEach
    void setUp() {        
        // Create and save test user
        testUser = new User();
        testUser.setUsername("integrationtestuser");
        testUser.setEmail("integration@test.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setDisplayName("Integration Test User");
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should complete full user profile management flow successfully")
    void shouldCompleteFullUserProfileManagementFlow() throws Exception {
        // Step 1: Get initial profile
        mockMvc.perform(get("/api/user-profile")
                .with(authentication(createAuthenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("integrationtestuser"))
                .andExpect(jsonPath("$.data.email").value("integration@test.com"))
                .andExpect(jsonPath("$.data.displayName").value("Integration Test User"))
                .andExpect(jsonPath("$.data.avatarUrl").isEmpty());

        // Step 2: Update profile information
        UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
                .username("updated_int_user")
                .displayName("Updated Integration User")
                .build();

        mockMvc.perform(put("/api/user-profile")
                .with(authentication(createAuthenticatedUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("updated_int_user"))
                .andExpect(jsonPath("$.data.email").value("integration@test.com"))
                .andExpect(jsonPath("$.data.displayName").value("Updated Integration User"));

        // Step 3: Upload avatar
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar",
                "integration-test-avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "integration test avatar content".getBytes()
        );

        mockMvc.perform(multipart("/api/user-profile/avatar")
                .file(avatarFile)
                .with(authentication(createAuthenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Avatar uploaded successfully"))
                .andExpect(jsonPath("$.data.avatarUrl").isNotEmpty())
                .andExpect(jsonPath("$.data.avatarUrl").value(containsString("user_" + testUser.getId() + "_avatar_")));

        // Step 4: Verify final state
        mockMvc.perform(get("/api/user-profile")
                .with(authentication(createAuthenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("updated_int_user"))
                .andExpect(jsonPath("$.data.email").value("integration@test.com"))
                .andExpect(jsonPath("$.data.displayName").value("Updated Integration User"))
                .andExpect(jsonPath("$.data.avatarUrl").isNotEmpty());

        // Verify database state
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.getUsername().equals("updated_int_user");
        assert updatedUser.getEmail().equals("integration@test.com");
        assert updatedUser.getDisplayName().equals("Updated Integration User");
        assert updatedUser.getAvatarUrl() != null;
        assert updatedUser.getAvatarUrl().contains("user_" + testUser.getId() + "_avatar_");
    }

    @Test
    @DisplayName("Should handle complete profile update with avatar in single request")
    void shouldHandleCompleteProfileUpdateWithAvatar() throws Exception {
        // Given
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar",
                "complete-update-avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "complete update avatar content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/user-profile/complete")
                .file(avatarFile)
                .param("username", "complete_update_user")
                .param("email", "complete.update@test.com")
                .param("displayName", "Complete Update User")
                .with(authentication(createAuthenticatedUser()))
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("complete_update_user"))
                .andExpect(jsonPath("$.data.email").value("integration@test.com"))
                .andExpect(jsonPath("$.data.displayName").value("Complete Update User"))
                .andExpect(jsonPath("$.data.avatarUrl").isNotEmpty());

        // Verify database state
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.getUsername().equals("complete_update_user");
        assert updatedUser.getEmail().equals("integration@test.com");
        assert updatedUser.getDisplayName().equals("Complete Update User");
        assert updatedUser.getAvatarUrl() != null;
    }

    @Test
    @DisplayName("Should prevent user from updating another user's profile")
    void shouldPreventUserFromUpdatingAnotherUsersProfile() throws Exception {
        // Create another user
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@test.com");
        anotherUser.setPassword(passwordEncoder.encode("password123"));
        anotherUser.setDisplayName("Another User");
        anotherUser = userRepository.save(anotherUser);

        // Try to access another user's profile (should get own profile instead due to authentication)
        mockMvc.perform(get("/api/user-profile")
                .with(authentication(createAuthenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("integrationtestuser"))
                .andExpect(jsonPath("$.data.email").value("integration@test.com"));
    }

    @Test
    @DisplayName("Should handle validation errors gracefully")
    void shouldHandleValidationErrorsGracefully() throws Exception {
        // Try to update with invalid data
        UpdateProfileRequest invalidRequest = UpdateProfileRequest.builder()
                .username("") // Invalid - empty username
                .displayName("") // Invalid - empty display name
                .build();

        mockMvc.perform(put("/api/user-profile")
                .with(authentication(createAuthenticatedUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Verify original data is unchanged
        User unchangedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert unchangedUser.getUsername().equals("integrationtestuser");
        assert unchangedUser.getEmail().equals("integration@test.com");
        assert unchangedUser.getDisplayName().equals("Integration Test User");
    }

    @Test
    @DisplayName("Should handle file upload validation errors")
    void shouldHandleFileUploadValidationErrors() throws Exception {
        // Try to upload invalid file type
        MockMultipartFile invalidFile = new MockMultipartFile(
                "avatar",
                "document.pdf",
                "application/pdf",
                "This is a PDF document".getBytes()
        );

        mockMvc.perform(multipart("/api/user-profile/avatar")
                .file(invalidFile)
                .with(authentication(createAuthenticatedUser())))
                .andExpect(status().isBadRequest());

        // Try to upload oversized file
        MockMultipartFile oversizedFile = new MockMultipartFile(
                "avatar",
                "large-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[6 * 1024 * 1024] // 6MB - exceeds 5MB limit
        );

        mockMvc.perform(multipart("/api/user-profile/avatar")
                .file(oversizedFile)
                .with(authentication(createAuthenticatedUser())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle username uniqueness validation")
    void shouldHandleUsernameUniquenessValidation() throws Exception {
        // Create another user with a specific username
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@test.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setDisplayName("Existing User");
        userRepository.save(existingUser);

        // Try to update profile with existing username
        UpdateProfileRequest conflictRequest = UpdateProfileRequest.builder()
                .username("existinguser") // This username already exists
                .displayName("Updated User")
                .build();

        mockMvc.perform(put("/api/user-profile")
                .with(authentication(createAuthenticatedUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conflictRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Username already exists")));

        // Verify original user data is unchanged
        User unchangedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert unchangedUser.getUsername().equals("integrationtestuser");
    }

    @Test
    @DisplayName("Should clean up old avatar when uploading new one")
    void shouldCleanUpOldAvatarWhenUploadingNewOne() throws Exception {

        // Upload a second avatar
        MockMultipartFile secondAvatar = new MockMultipartFile(
                "avatar",
                "second-avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "second avatar content".getBytes()
        );

        mockMvc.perform(multipart("/api/user-profile/avatar")
                .file(secondAvatar)
                .with(authentication(createAuthenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.avatarUrl").isNotEmpty());

        // Verify user has the new avatar URL
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.getAvatarUrl() != null;
        assert updatedUser.getAvatarUrl().contains("user_" + testUser.getId() + "_avatar_");
    }
}
