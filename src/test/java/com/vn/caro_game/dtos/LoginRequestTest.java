package com.vn.caro_game.dtos;

import com.vn.caro_game.dtos.request.LoginRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginRequest DTO Tests")
class LoginRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid LoginRequest with username and password")
    void shouldCreateValidLoginRequest() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("Password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.getUsername()).isEqualTo("testuser");
        assertThat(request.getPassword()).isEqualTo("Password123");
    }

    @Test
    @DisplayName("Should validate username is not blank")
    void shouldValidateUsernameNotBlank() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername(""); // Empty string triggers multiple validations
        request.setPassword("Password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then - Multiple validation errors for empty username
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(3); // @NotBlank, @Size, and @Pattern will all fail
    }

    @Test
    @DisplayName("Should validate username is not null")
    void shouldValidateUsernameNotNull() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername(null);
        request.setPassword("Password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should validate password is not blank")
    void shouldValidatePasswordNotBlank() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword(""); // Empty string triggers multiple validations

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then - Multiple validation errors for empty password
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(3); // @NotBlank, @Size, and @Pattern will all fail
    }

    @Test
    @DisplayName("Should validate password is not null")
    void shouldValidatePasswordNotNull() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword(null);

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should validate username length constraints")
    void shouldValidateUsernameLength() {
        // Given - too short username
        LoginRequest request = new LoginRequest();
        request.setUsername("ab");
        request.setPassword("Password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).contains("Username must be between 3 and 50 characters");
    }

    @Test
    @DisplayName("Should validate password length constraints")
    void shouldValidatePasswordLength() {
        // Given - too short password
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then - Short password triggers both size and pattern validations
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(2);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Password must be between 8 and 128 characters",
                        "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
                );
    }

    @Test
    @DisplayName("Should accept valid username with allowed characters")
    void shouldAcceptValidUsernameWithAllowedCharacters() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("test_user123");
        request.setPassword("Password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should accept maximum length username")
    void shouldAcceptMaximumLengthUsername() {
        // Given - 50 character username
        String longUsername = "a".repeat(50);
        LoginRequest request = new LoginRequest();
        request.setUsername(longUsername);
        request.setPassword("Password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject username that is too long")
    void shouldRejectUsernameTooLong() {
        // Given - 51 character username
        String longUsername = "a".repeat(51);
        LoginRequest request = new LoginRequest();
        request.setUsername(longUsername);
        request.setPassword("Password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should validate username pattern")
    void shouldValidateUsernamePattern() {
        // Given - username with invalid characters
        LoginRequest request = new LoginRequest();
        request.setUsername("test user@"); // spaces and @ not allowed
        request.setPassword("Password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).contains("Username can only contain letters, numbers, dots, underscores, and hyphens");
    }

    @Test
    @DisplayName("Should accept valid password with minimum requirements")
    void shouldAcceptValidPasswordWithMinimumRequirements() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("Abcd1234"); // 8 chars, has upper, lower, digit

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject weak password without uppercase")
    void shouldRejectWeakPasswordWithoutUppercase() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("abcd1234"); // no uppercase

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).contains("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
    }

    @Test
    @DisplayName("Should reject weak password without lowercase")
    void shouldRejectWeakPasswordWithoutLowercase() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("ABCD1234"); // no lowercase

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).contains("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
    }

    @Test
    @DisplayName("Should reject weak password without digit")
    void shouldRejectWeakPasswordWithoutDigit() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("AbcdEfgh"); // no digit

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).contains("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
    }
}
