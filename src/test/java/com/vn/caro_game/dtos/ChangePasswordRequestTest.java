package com.vn.caro_game.dtos;

import com.vn.caro_game.dtos.request.ChangePasswordRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChangePasswordRequest DTO Tests")
class ChangePasswordRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid ChangePasswordRequest")
    void shouldCreateValidChangePasswordRequest() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentPassword123");
        request.setNewPassword("NewPassword123");
        request.setOtp("123456");

        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.getCurrentPassword()).isEqualTo("currentPassword123");
        assertThat(request.getNewPassword()).isEqualTo("NewPassword123");
        assertThat(request.getOtp()).isEqualTo("123456");
    }

    @Test
    @DisplayName("Should validate current password is not blank")
    void shouldValidateCurrentPasswordNotBlank() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("");
        request.setNewPassword("NewPassword123");
        request.setOtp("123456");

        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Current password cannot be blank");
    }

    @Test
    @DisplayName("Should validate current password is not null")
    void shouldValidateCurrentPasswordNotNull() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(null);
        request.setNewPassword("NewPassword123");
        request.setOtp("123456");

        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Current password cannot be blank");
    }

    @Test
    @DisplayName("Should validate new password is not blank")
    void shouldValidateNewPasswordNotBlank() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentPassword123");
        request.setNewPassword("");
        request.setOtp("123456");

        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(3);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "New password cannot be blank",
                        "Password must be between 8 and 128 characters",
                        "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
                );
    }

    @Test
    @DisplayName("Should validate new password is not null")
    void shouldValidateNewPasswordNotNull() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentPassword123");
        request.setNewPassword(null);
        request.setOtp("123456");

        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("New password cannot be blank");
    }

    @Test
    @DisplayName("Should validate new password minimum length")
    void shouldValidateNewPasswordMinLength() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentPassword123");
        request.setNewPassword("New12"); // 5 ký tự, không đủ 8
        request.setOtp("123456");

        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

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
    @DisplayName("Should validate new password pattern - missing uppercase")
    void shouldValidateNewPasswordPatternMissingUppercase() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentPassword123");
        request.setNewPassword("newpassword123"); // không có chữ hoa
        request.setOtp("123456");

        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
    }

    @Test
    @DisplayName("Should validate new password pattern - missing lowercase")
    void shouldValidateNewPasswordPatternMissingLowercase() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentPassword123");
        request.setNewPassword("NEWPASSWORD123"); // không có chữ thường
        request.setOtp("123456");

        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
    }

    @Test
    @DisplayName("Should validate new password pattern - missing digit")
    void shouldValidateNewPasswordPatternMissingDigit() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentPassword123");
        request.setNewPassword("NewPassword"); // không có số
        request.setOtp("123456");

        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
    }

    @Test
    @DisplayName("Should validate OTP is not blank")
    void shouldValidateOtpNotBlank() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentPassword123");
        request.setNewPassword("NewPassword123");
        request.setOtp("");

        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "OTP cannot be blank",
                        "OTP must be 6 digits"
                );
    }

    @Test
    @DisplayName("Should validate OTP is not null")
    void shouldValidateOtpNotNull() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentPassword123");
        request.setNewPassword("NewPassword123");
        request.setOtp(null);

        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("OTP cannot be blank");
    }

    @Test
    @DisplayName("Should validate OTP pattern")
    void shouldValidateOtpPattern() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentPassword123");
        request.setNewPassword("NewPassword123");
        request.setOtp("12345a"); // chứa chữ cái

        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("OTP must be 6 digits");
    }

    @Test
    @DisplayName("Should validate OTP length")
    void shouldValidateOtpLength() {
        String[] invalidOtps = {
                "12345",    // 5 digits
                "1234567",  // 7 digits
                "123",      // 3 digits
        };

        for (String otp : invalidOtps) {
            // Given
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("currentPassword123");
            request.setNewPassword("NewPassword123");
            request.setOtp(otp);

            // When
            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations)
                    .as("OTP %s should be invalid", otp)
                    .hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("OTP must be 6 digits");
        }
    }

    @Test
    @DisplayName("Should validate all invalid fields")
    void shouldValidateAllInvalidFields() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("");
        request.setNewPassword("weak"); // quá ngắn và không đủ mạnh
        request.setOtp("123a56"); // sai format

        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(4);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Current password cannot be blank",
                        "Password must contain at least one uppercase letter, one lowercase letter, and one digit",
                        "Password must be between 8 and 128 characters",
                        "OTP must be 6 digits"
                );
    }
}
