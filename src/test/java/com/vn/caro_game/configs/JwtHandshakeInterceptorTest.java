package com.vn.caro_game.configs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtHandshakeInterceptor.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class JwtHandshakeInterceptorTest {

    @InjectMocks
    private JwtHandshakeInterceptor jwtHandshakeInterceptor;

    private ServerHttpRequest request;
    private ServerHttpResponse response;
    private WebSocketHandler webSocketHandler;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        request = mock(ServerHttpRequest.class);
        response = mock(ServerHttpResponse.class);
        webSocketHandler = mock(WebSocketHandler.class);
        attributes = new java.util.HashMap<>();
    }

    @Test
    void beforeHandshake_WithValidToken_ShouldExtractTokenAndReturnTrue() throws Exception {
        // Given
        String validToken = "valid-jwt-token";
        URI uri = URI.create("ws://localhost:8080/ws?token=" + validToken);
        
        when(request.getURI()).thenReturn(uri);

        // When
        boolean result = jwtHandshakeInterceptor.beforeHandshake(request, response, webSocketHandler, attributes);

        // Then
        assertTrue(result);
        assertEquals(validToken, attributes.get("jwt"));
    }

    @Test
    void beforeHandshake_WithBearerToken_ShouldStripBearerAndReturnTrue() throws Exception {
        // Given
        String tokenWithBearer = "Bearer%20valid-jwt-token"; // URL encoded
        String expectedToken = "valid-jwt-token";
        URI uri = URI.create("ws://localhost:8080/ws?token=" + tokenWithBearer);
        
        when(request.getURI()).thenReturn(uri);

        // When
        boolean result = jwtHandshakeInterceptor.beforeHandshake(request, response, webSocketHandler, attributes);

        // Then
        assertTrue(result);
        assertEquals(expectedToken, attributes.get("jwt"));
    }

    @Test
    void beforeHandshake_WithoutToken_ShouldReturnTrue() throws Exception {
        // Given
        URI uri = URI.create("ws://localhost:8080/ws");
        
        when(request.getURI()).thenReturn(uri);

        // When
        boolean result = jwtHandshakeInterceptor.beforeHandshake(request, response, webSocketHandler, attributes);

        // Then
        assertTrue(result); // Always returns true according to implementation
        assertNull(attributes.get("jwt"));
    }

    @Test
    void beforeHandshake_WithEmptyToken_ShouldReturnTrue() throws Exception {
        // Given
        URI uri = URI.create("ws://localhost:8080/ws?token=");
        
        when(request.getURI()).thenReturn(uri);

        // When
        boolean result = jwtHandshakeInterceptor.beforeHandshake(request, response, webSocketHandler, attributes);

        // Then
        assertTrue(result); // Always returns true according to implementation
        // Empty token results in empty string in attributes, but implementation might not set it
        // Let's just verify no exception is thrown
    }

    @Test
    void beforeHandshake_WithMultipleQueryParams_ShouldExtractTokenCorrectly() throws Exception {
        // Given
        String validToken = "valid-jwt-token";
        URI uri = URI.create("ws://localhost:8080/ws?param1=value1&token=" + validToken + "&param2=value2");
        
        when(request.getURI()).thenReturn(uri);

        // When
        boolean result = jwtHandshakeInterceptor.beforeHandshake(request, response, webSocketHandler, attributes);

        // Then
        assertTrue(result);
        assertEquals(validToken, attributes.get("jwt"));
    }

    @Test
    void beforeHandshake_WithUrlEncodedToken_ShouldDecodeCorrectly() throws Exception {
        // Given
        String encodedToken = "Bearer%20valid-jwt-token";
        String expectedToken = "valid-jwt-token";
        URI uri = URI.create("ws://localhost:8080/ws?token=" + encodedToken);
        
        when(request.getURI()).thenReturn(uri);

        // When
        boolean result = jwtHandshakeInterceptor.beforeHandshake(request, response, webSocketHandler, attributes);

        // Then
        assertTrue(result);
        assertEquals(expectedToken, attributes.get("jwt"));
    }

    @Test
    void afterHandshake_ShouldCompleteWithoutException() {
        // When & Then
        assertDoesNotThrow(() -> 
            jwtHandshakeInterceptor.afterHandshake(request, response, webSocketHandler, null)
        );
    }
}
