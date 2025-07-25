package com.vn.caro_game.dtos;

import com.vn.caro_game.dtos.request.UserCreation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserCreation DTO Tests")
class UserCreationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid UserCreation")
    void shouldCreateValidUserCreation() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername("testuser");
        request.setPassword("Password123");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.getUsername()).isEqualTo("testuser");
        assertThat(request.getPassword()).isEqualTo("Password123");
        assertThat(request.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should validate username is not blank")
    void shouldValidateUsernameNotBlank() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername("");
        request.setPassword("Password123");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(3);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Username cannot be blank",
                        "Username must be between 3 and 50 characters",
                        "Username can only contain letters, numbers, dots, underscores, and hyphens"
                );
    }

    @Test
    @DisplayName("Should validate username is not null")
    void shouldValidateUsernameNotNull() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername(null);
        request.setPassword("Password123");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Username cannot be blank");
    }

    @Test
    @DisplayName("Should validate username minimum length")
    void shouldValidateUsernameMinLength() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername("ab"); // 2 ký tự
        request.setPassword("Password123");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Username must be between 3 and 50 characters");
    }

    @Test
    @DisplayName("Should validate username maximum length")
    void shouldValidateUsernameMaxLength() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername("a".repeat(51)); // 51 ký tự
        request.setPassword("Password123");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Username must be between 3 and 50 characters");
    }

    @Test
    @DisplayName("Should validate username pattern")
    void shouldValidateUsernamePattern() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername("test user@"); // chứa space và @ không được phép
        request.setPassword("Password123");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Username can only contain letters, numbers, dots, underscores, and hyphens");
    }

    @Test
    @DisplayName("Should validate password is not blank")
    void shouldValidatePasswordNotBlank() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername("testuser");
        request.setPassword("");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(3);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Password cannot be blank",
                        "Password must be between 8 and 128 characters",
                        "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
                );
    }

    @Test
    @DisplayName("Should validate password is not null")
    void shouldValidatePasswordNotNull() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername("testuser");
        request.setPassword(null);
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Password cannot be blank");
    }

    @Test
    @DisplayName("Should validate password minimum length")
    void shouldValidatePasswordMinLength() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername("testuser");
        request.setPassword("Pass12"); // 6 ký tự, không đủ 8
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Password must be between 8 and 128 characters",
                        "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
                );
    }

    @Test
    @DisplayName("Should validate password pattern - missing uppercase")
    void shouldValidatePasswordPatternMissingUppercase() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername("testuser");
        request.setPassword("password123"); // không có chữ hoa
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
    }

    @Test
    @DisplayName("Should validate password pattern - missing lowercase")
    void shouldValidatePasswordPatternMissingLowercase() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername("testuser");
        request.setPassword("PASSWORD123"); // không có chữ thường
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
    }

    @Test
    @DisplayName("Should validate password pattern - missing digit")
    void shouldValidatePasswordPatternMissingDigit() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername("testuser");
        request.setPassword("Password"); // không có số
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
    }

    @Test
    @DisplayName("Should validate email is not blank")
    void shouldValidateEmailNotBlank() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername("testuser");
        request.setPassword("Password123");
        request.setEmail("");

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Email cannot be blank");
    }

    @Test
    @DisplayName("Should validate email is not null")
    void shouldValidateEmailNotNull() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername("testuser");
        request.setPassword("Password123");
        request.setEmail(null);

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Email cannot be blank");
    }

    @Test
    @DisplayName("Should validate email format")
    void shouldValidateEmailFormat() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername("testuser");
        request.setPassword("Password123");
        request.setEmail("invalid-email");

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Email format is invalid");
    }

    @Test
    @DisplayName("Should validate email maximum length")
    void shouldValidateEmailMaxLength() {
        // Given
        UserCreation request = new UserCreation();
        request.setUsername("testuser");
        request.setPassword("Password123");
        // Tạo email quá dài (> 254 ký tự)
        String longEmail = "a".repeat(250) + "@test.com";
        request.setEmail(longEmail);

        // When
        Set<ConstraintViolation<UserCreation>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Email must not exceed 254 characters",
                        "Email format is invalid"
                );
    }
}
