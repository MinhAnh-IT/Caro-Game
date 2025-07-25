package com.vn.caro_game.dtos.request;

import com.vn.caro_game.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Login request with username and password")
public class LoginRequest {
    
    @NotBlank(message = ValidationConstants.USERNAME_BLANK_MESSAGE)
    @Size(min = ValidationConstants.USERNAME_MIN_LENGTH, max = ValidationConstants.USERNAME_MAX_LENGTH, 
          message = ValidationConstants.USERNAME_SIZE_MESSAGE)
    @Pattern(regexp = ValidationConstants.USERNAME_PATTERN, message = ValidationConstants.USERNAME_PATTERN_MESSAGE)
    @Schema(description = "User's username", example = "john_doe", required = true)
    String username;
    
    @NotBlank(message = ValidationConstants.PASSWORD_BLANK_MESSAGE)
    @Size(min = ValidationConstants.PASSWORD_MIN_LENGTH, max = ValidationConstants.PASSWORD_MAX_LENGTH, 
          message = ValidationConstants.PASSWORD_SIZE_MESSAGE)
    @Pattern(regexp = ValidationConstants.PASSWORD_PATTERN, message = ValidationConstants.PASSWORD_PATTERN_MESSAGE)
    @Schema(description = "User's password", example = "SecurePassword123", required = true)
    String password;
}
