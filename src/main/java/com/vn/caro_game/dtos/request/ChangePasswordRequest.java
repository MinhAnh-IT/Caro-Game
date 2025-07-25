package com.vn.caro_game.dtos.request;

import com.vn.caro_game.constants.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    @NotBlank(message = ValidationConstants.CURRENT_PASSWORD_BLANK_MESSAGE)
    String currentPassword;
    
    @NotBlank(message = ValidationConstants.NEW_PASSWORD_BLANK_MESSAGE)
    @Size(min = ValidationConstants.PASSWORD_MIN_LENGTH, max = ValidationConstants.PASSWORD_MAX_LENGTH, 
          message = ValidationConstants.PASSWORD_SIZE_MESSAGE)
    @Pattern(regexp = ValidationConstants.PASSWORD_PATTERN, message = ValidationConstants.PASSWORD_PATTERN_MESSAGE)
    String newPassword;
    
    @NotBlank(message = ValidationConstants.OTP_BLANK_MESSAGE)
    @Pattern(regexp = ValidationConstants.OTP_PATTERN, message = ValidationConstants.OTP_PATTERN_MESSAGE)
    String otp;
}
