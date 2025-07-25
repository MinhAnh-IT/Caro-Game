package com.vn.caro_game.dtos;

import com.vn.caro_game.dtos.response.AuthResponse;
import com.vn.caro_game.dtos.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthResponse DTO Tests")
class AuthResponseTest {

    @Test
    @DisplayName("Should create AuthResponse with builder")
    void shouldCreateAuthResponseWithBuilder() {
        // Given
        String accessToken = "jwt-access-token";
        String refreshToken = "jwt-refresh-token";
        String tokenType = "Bearer";
        Long expiresIn = 3600L;

        // When
        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .expiresIn(expiresIn)
                .build();

        // Then
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getTokenType()).isEqualTo(tokenType);
        assertThat(response.getExpiresIn()).isEqualTo(expiresIn);
    }

    @Test
    @DisplayName("Should set and get access token")
    void shouldSetAndGetAccessToken() {
        // Given
        AuthResponse response = AuthResponse.builder().build();
        String accessToken = "test-access-token";

        // When
        response.setAccessToken(accessToken);

        // Then
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
    }

    @Test
    @DisplayName("Should set and get refresh token")
    void shouldSetAndGetRefreshToken() {
        // Given
        AuthResponse response = AuthResponse.builder().build();
        String refreshToken = "test-refresh-token";

        // When
        response.setRefreshToken(refreshToken);

        // Then
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("Should set and get token type")
    void shouldSetAndGetTokenType() {
        // Given
        AuthResponse response = AuthResponse.builder().build();
        String tokenType = "Bearer";

        // When
        response.setTokenType(tokenType);

        // Then
        assertThat(response.getTokenType()).isEqualTo(tokenType);
    }

    @Test
    @DisplayName("Should set and get expires in")
    void shouldSetAndGetExpiresIn() {
        // Given
        AuthResponse response = AuthResponse.builder().build();
        Long expiresIn = 7200L;

        // When
        response.setExpiresIn(expiresIn);

        // Then
        assertThat(response.getExpiresIn()).isEqualTo(expiresIn);
    }

    @Test
    @DisplayName("Should set and get user response")
    void shouldSetAndGetUserResponse() {
        // Given
        AuthResponse response = AuthResponse.builder().build();
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        // When
        response.setUser(userResponse);

        // Then
        assertThat(response.getUser()).isEqualTo(userResponse);
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should create complete AuthResponse")
    void shouldCreateCompleteAuthResponse() {
        // Given
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        // When
        AuthResponse response = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(userResponse)
                .build();

        // Then
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should have null fields when not set")
    void shouldHaveNullFieldsWhenNotSet() {
        // When
        AuthResponse response = AuthResponse.builder().build();

        // Then
        assertThat(response.getAccessToken()).isNull();
        assertThat(response.getRefreshToken()).isNull();
        assertThat(response.getTokenType()).isNull();
        assertThat(response.getExpiresIn()).isNull();
        assertThat(response.getUser()).isNull();
    }
}
