package com.vn.caro_game.controllers;

import com.vn.caro_game.configs.CustomUserDetails;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.services.impl.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthController focusing on CustomUserDetails integration.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setDisplayName("Test User");
        testUser.setPassword("hashedPassword");
        testUser.setCreatedAt(LocalDateTime.now());

        customUserDetails = new CustomUserDetails(testUser);
    }

    @Test
    @DisplayName("CustomUserDetails should provide correct user information")
    void customUserDetailsShouldProvideCorrectUserInformation() {
        // Given & When
        Long userId = customUserDetails.getUserId();
        String email = customUserDetails.getEmail();
        String username = customUserDetails.getUsername();
        String displayName = customUserDetails.getDisplayName();

        // Then
        assertEquals(1L, userId);
        assertEquals("test@example.com", email);
        assertEquals("testuser", username);
        assertEquals("Test User", displayName);
    }

    @Test
    @DisplayName("requestChangePasswordOtp should extract email from CustomUserDetails")
    void requestChangePasswordOtp_ShouldExtractEmailFromCustomUserDetails() {
        // Given
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        SecurityContextHolder.setContext(securityContext);

        doNothing().when(authService).requestChangePasswordOtp("test@example.com");

        // When
        authController.requestChangePasswordOtp();

        // Then
        verify(authService).requestChangePasswordOtp("test@example.com");
        // Verify that the email was correctly extracted from CustomUserDetails
    }

    @Test
    @DisplayName("CustomUserDetails should be properly constructed from User entity")
    void customUserDetails_ShouldBeProperlyConstructedFromUserEntity() {
        // Given
        User user = new User();
        user.setId(2L);
        user.setUsername("anotheruser");
        user.setEmail("another@example.com");
        user.setDisplayName("Another User");

        // When
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // Then
        assertEquals(2L, userDetails.getUserId());
        assertEquals("another@example.com", userDetails.getEmail());
        assertEquals("anotheruser", userDetails.getUsername());
        assertEquals("Another User", userDetails.getDisplayName());
        assertNotNull(userDetails.getAuthorities());
        assertTrue(userDetails.getAuthorities().size() > 0);
    }

    @Test
    @DisplayName("CustomUserDetails should handle null values gracefully")
    void customUserDetails_ShouldHandleNullValuesGracefully() {
        // Given
        User user = new User();
        user.setId(3L);
        user.setUsername("userwithnulls");
        user.setEmail("user@example.com");
        // displayName and avatarUrl are null

        // When
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // Then
        assertEquals(3L, userDetails.getUserId());
        assertEquals("user@example.com", userDetails.getEmail());
        assertEquals("userwithnulls", userDetails.getUsername());
        assertNull(userDetails.getDisplayName());
        assertNull(userDetails.getAvatarUrl());
    }
}
