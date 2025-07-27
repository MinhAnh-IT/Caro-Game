package com.vn.caro_game.services;

import com.vn.caro_game.constants.ApplicationConstants;
import com.vn.caro_game.constants.EmailConstants;
import com.vn.caro_game.constants.MessageConstants;
import com.vn.caro_game.dtos.request.*;
import com.vn.caro_game.dtos.response.AuthResponse;
import com.vn.caro_game.dtos.response.UserResponse;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.StatusCode;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.integrations.email.EmailService;
import com.vn.caro_game.integrations.jwt.JwtService;
import com.vn.caro_game.integrations.redis.OtpService;
import com.vn.caro_game.mappers.UserMapper;
import com.vn.caro_game.repositories.UserRepository;
import com.vn.caro_game.services.impl.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @Mock
    private OtpService otpService;

    private AuthService authService;

    private User testUser;
    private UserCreation userCreation;
    private UserResponse userResponse;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, userMapper, passwordEncoder, 
                                    jwtService, emailService, otpService);
        
        ReflectionTestUtils.setField(authService, "maxOtpAttempts", 3);

        // Setup test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");

        userCreation = new UserCreation();
        userCreation.setEmail("test@example.com");
        userCreation.setUsername("testuser");
        userCreation.setPassword("password123");

        userResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() {
        // Given
        when(userRepository.existsByEmail(userCreation.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(userCreation.getUsername())).thenReturn(false);
        when(userMapper.toEntity(userCreation)).thenReturn(testUser);
        when(passwordEncoder.encode(userCreation.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);

        // When
        UserResponse result = authService.register(userCreation);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(userCreation.getEmail());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendWelcomeEmail(testUser.getEmail(), testUser.getUsername());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(userCreation.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(userCreation))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("statusCode", StatusCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(userCreation.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(userCreation.getUsername())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(userCreation))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("statusCode", StatusCode.USERNAME_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfullyWithValidCredentials() {
        // Given
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(testUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token");
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getTokenType()).isEqualTo(MessageConstants.TOKEN_TYPE_BEARER);
        assertThat(result.getExpiresIn()).isEqualTo(ApplicationConstants.DEFAULT_ACCESS_TOKEN_EXPIRATION);
        assertThat(result.getUser()).isEqualTo(userResponse);
    }

    @Test
    @DisplayName("Should throw exception when login with invalid username")
    void shouldThrowExceptionWhenLoginWithInvalidUsername() {
        // Given
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("statusCode", StatusCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("Should throw exception when login with invalid password")
    void shouldThrowExceptionWhenLoginWithInvalidPassword() {
        // Given
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("statusCode", StatusCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("Should process forgot password successfully")
    void shouldProcessForgotPasswordSuccessfully() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
        when(otpService.isOtpAttemptsExceeded(request.getEmail(), MessageConstants.RESET_PASSWORD_OTP_TYPE)).thenReturn(false);
        when(otpService.generateOtp()).thenReturn("123456");

        // When
        authService.forgotPassword(request);

        // Then
        verify(otpService).storeOtp(request.getEmail(), "123456", MessageConstants.RESET_PASSWORD_OTP_TYPE);
        verify(emailService).sendOtpEmail(request.getEmail(), "123456", EmailConstants.PASSWORD_RESET_SUBJECT);
    }

    @Test
    @DisplayName("Should throw exception when forgot password with non-existent email")
    void shouldThrowExceptionWhenForgotPasswordWithNonExistentEmail() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("nonexistent@example.com");
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.forgotPassword(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("statusCode", StatusCode.EMAIL_NOT_FOUND);
    }

    @Test
    @DisplayName("Should throw exception when OTP attempts exceeded for forgot password")
    void shouldThrowExceptionWhenOtpAttemptsExceededForForgotPassword() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
        when(otpService.isOtpAttemptsExceeded(request.getEmail(), MessageConstants.RESET_PASSWORD_OTP_TYPE)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.forgotPassword(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("statusCode", StatusCode.OTP_ATTEMPTS_EXCEEDED);
    }

    @Test
    @DisplayName("Should reset password successfully")
    void shouldResetPasswordSuccessfully() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("newPassword123");
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
        when(otpService.validateOtp(request.getEmail(), request.getOtp(), MessageConstants.RESET_PASSWORD_OTP_TYPE)).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPassword");

        // When
        authService.resetPassword(request);

        // Then
        verify(userRepository).updatePassword(request.getEmail(), "encodedNewPassword");
        verify(otpService).removeOtp(request.getEmail(), MessageConstants.RESET_PASSWORD_OTP_TYPE);
        verify(emailService).sendPasswordChangeNotification(request.getEmail(), testUser.getUsername());
    }

    @Test
    @DisplayName("Should throw exception when reset password with invalid OTP")
    void shouldThrowExceptionWhenResetPasswordWithInvalidOtp() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("invalid-otp");
        request.setNewPassword("newPassword123");
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
        when(otpService.validateOtp(request.getEmail(), request.getOtp(), MessageConstants.RESET_PASSWORD_OTP_TYPE)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("statusCode", StatusCode.INVALID_OTP);
        
        verify(otpService).incrementOtpAttempts(request.getEmail(), MessageConstants.RESET_PASSWORD_OTP_TYPE);
    }

    @Test
    @DisplayName("Should request change password OTP successfully")
    void shouldRequestChangePasswordOtpSuccessfully() {
        // Given
        String email = "test@example.com";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(otpService.isOtpAttemptsExceeded(email, MessageConstants.CHANGE_PASSWORD_OTP_TYPE)).thenReturn(false);
        when(otpService.generateOtp()).thenReturn("123456");

        // When
        authService.requestChangePasswordOtp(email);

        // Then
        verify(otpService).storeOtp(email, "123456", MessageConstants.CHANGE_PASSWORD_OTP_TYPE);
        verify(emailService).sendOtpEmail(email, "123456", EmailConstants.PASSWORD_CHANGE_VERIFICATION_SUBJECT);
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentPassword");
        request.setNewPassword("newPassword123");
        request.setOtp("123456");
        String email = "test@example.com";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getCurrentPassword(), testUser.getPassword())).thenReturn(true);
        when(otpService.validateOtp(email, request.getOtp(), MessageConstants.CHANGE_PASSWORD_OTP_TYPE)).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPassword");

        // When
        authService.changePassword(request, email);

        // Then
        verify(userRepository).updatePassword(email, "encodedNewPassword");
        verify(otpService).removeOtp(email, MessageConstants.CHANGE_PASSWORD_OTP_TYPE);
        verify(emailService).sendPasswordChangeNotification(email, testUser.getUsername());
    }

    @Test
    @DisplayName("Should throw exception when change password with wrong current password")
    void shouldThrowExceptionWhenChangePasswordWithWrongCurrentPassword() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongCurrentPassword");
        request.setNewPassword("newPassword123");
        request.setOtp("123456");
        String email = "test@example.com";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getCurrentPassword(), testUser.getPassword())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.changePassword(request, email))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("statusCode", StatusCode.CURRENT_PASSWORD_INCORRECT);
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");
        
        when(jwtService.isTokenExpired(request.getRefreshToken())).thenReturn(false);
        when(jwtService.isTokenBlacklisted(request.getRefreshToken())).thenReturn(false);
        when(jwtService.extractEmail(request.getRefreshToken())).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("new-refresh-token");
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);

        // When
        AuthResponse result = authService.refreshToken(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");
        verify(jwtService).invalidateToken(request.getRefreshToken());
    }

    @Test
    @DisplayName("Should throw exception when refresh with expired token")
    void shouldThrowExceptionWhenRefreshWithExpiredToken() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired-token");
        
        when(jwtService.isTokenExpired(request.getRefreshToken())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("statusCode", StatusCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("Should logout successfully")
    void shouldLogoutSuccessfully() {
        // Given
        String token = "valid-token";

        // When
        authService.logout(token);

        // Then
        verify(jwtService).invalidateToken(token);
    }
}
