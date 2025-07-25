package com.vn.caro_game.dtos.request;

import com.vn.caro_game.constants.ValidationConstants;
import jakarta.validation.constraints.Email;
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
public class UserCreation {
    @NotBlank(message = ValidationConstants.USERNAME_BLANK_MESSAGE)
    @Size(min = ValidationConstants.USERNAME_MIN_LENGTH, max = ValidationConstants.USERNAME_MAX_LENGTH, 
          message = ValidationConstants.USERNAME_SIZE_MESSAGE)
    @Pattern(regexp = ValidationConstants.USERNAME_PATTERN, message = ValidationConstants.USERNAME_PATTERN_MESSAGE)
    String username;
    
    @NotBlank(message = ValidationConstants.PASSWORD_BLANK_MESSAGE)
    @Size(min = ValidationConstants.PASSWORD_MIN_LENGTH, max = ValidationConstants.PASSWORD_MAX_LENGTH, 
          message = ValidationConstants.PASSWORD_SIZE_MESSAGE)
    @Pattern(regexp = ValidationConstants.PASSWORD_PATTERN, message = ValidationConstants.PASSWORD_PATTERN_MESSAGE)
    String password;
    
    @NotBlank(message = ValidationConstants.EMAIL_BLANK_MESSAGE)
    @Email(message = ValidationConstants.EMAIL_INVALID_MESSAGE)
    @Size(max = ValidationConstants.EMAIL_MAX_LENGTH, message = ValidationConstants.EMAIL_SIZE_MESSAGE)
    String email;
}
