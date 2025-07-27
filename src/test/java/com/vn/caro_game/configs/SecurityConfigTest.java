package com.vn.caro_game.configs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecurityConfig.
 * Tests the configuration beans and CORS settings.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // When
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Then
        assertNotNull(passwordEncoder);
        assertEquals("BCryptPasswordEncoder", passwordEncoder.getClass().getSimpleName());
    }

    @Test
    void passwordEncoder_ShouldEncodeAndMatchPasswords() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertFalse(passwordEncoder.matches("wrongPassword", encodedPassword));
    }

    @Test
    void corsConfigurationSource_ShouldReturnValidSource() {
        // When
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();

        // Then
        assertNotNull(corsConfigurationSource);
        assertTrue(corsConfigurationSource.getClass().getSimpleName().contains("UrlBased"));
    }

    @Test
    void corsConfiguration_ShouldAllowWildcardOrigins() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // When
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfig = corsConfigurationSource.getCorsConfiguration(request);

        // Then
        assertNotNull(corsConfig);
        var allowedOriginPatterns = corsConfig.getAllowedOriginPatterns();
        assertNotNull(allowedOriginPatterns);
        assertFalse(allowedOriginPatterns.isEmpty());
        assertTrue(allowedOriginPatterns.contains("*"));
    }

    @Test
    void corsConfiguration_ShouldAllowStandardHttpMethods() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // When
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfig = corsConfigurationSource.getCorsConfiguration(request);

        // Then
        assertNotNull(corsConfig);
        var allowedMethods = corsConfig.getAllowedMethods();
        assertNotNull(allowedMethods);
        assertTrue(allowedMethods.contains("GET"));
        assertTrue(allowedMethods.contains("POST"));
        assertTrue(allowedMethods.contains("PUT"));
        assertTrue(allowedMethods.contains("DELETE"));
        assertTrue(allowedMethods.contains("OPTIONS"));
    }

    @Test
    void corsConfiguration_ShouldAllowAllHeaders() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // When
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfig = corsConfigurationSource.getCorsConfiguration(request);

        // Then
        assertNotNull(corsConfig);
        var allowedHeaders = corsConfig.getAllowedHeaders();
        assertNotNull(allowedHeaders);
        assertTrue(allowedHeaders.contains("*"));
    }

    @Test
    void corsConfiguration_ShouldAllowCredentials() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // When
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfig = corsConfigurationSource.getCorsConfiguration(request);

        // Then
        assertNotNull(corsConfig);
        Boolean allowCredentials = corsConfig.getAllowCredentials();
        assertNotNull(allowCredentials);
        assertTrue(allowCredentials);
    }
}
