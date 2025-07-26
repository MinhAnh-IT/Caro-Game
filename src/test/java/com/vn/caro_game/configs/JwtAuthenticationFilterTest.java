package com.vn.caro_game.configs;

import com.vn.caro_game.integrations.jwt.JwtService;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void doFilterInternal_WithoutAuthorizationHeader_ShouldContinueFilterChain() throws Exception {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractEmail(anyString());
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_WithInvalidBearerToken_ShouldContinueFilterChain() throws Exception {
        // Given
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractEmail(anyString());
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldSetAuthentication() throws Exception {
        // Given
        String token = "valid.jwt.token";
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(token, email)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractEmail(token);
        verify(userRepository).findByEmail(email);
        verify(jwtService).isTokenValid(token, email);
        verify(securityContext).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws Exception {
        // Given
        String token = "invalid.jwt.token";
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(token, email)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractEmail(token);
        verify(userRepository).findByEmail(email);
        verify(jwtService).isTokenValid(token, email);
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNonExistentUser_ShouldNotSetAuthentication() throws Exception {
        // Given
        String token = "valid.jwt.token";
        String email = "nonexistent@example.com";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractEmail(token);
        verify(userRepository).findByEmail(email);
        verify(jwtService, never()).isTokenValid(anyString(), anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithExistingAuthentication_ShouldNotOverrideAuthentication() throws Exception {
        // Given
        String token = "valid.jwt.token";
        String email = "test@example.com";
        Authentication existingAuth = mock(Authentication.class);
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(securityContext.getAuthentication()).thenReturn(existingAuth);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractEmail(token);
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).isTokenValid(anyString(), anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithJwtServiceException_ShouldContinueFilterChain() throws Exception {
        // Given
        String token = "malformed.jwt.token";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractEmail(token)).thenThrow(new RuntimeException("Malformed JWT"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractEmail(token);
        verify(userRepository, never()).findByEmail(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }
}
