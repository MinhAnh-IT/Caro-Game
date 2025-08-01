package com.vn.caro_game.controllers;

import com.vn.caro_game.configs.CustomUserDetails;
import com.vn.caro_game.constants.UserProfileConstants;
import com.vn.caro_game.dtos.request.UpdateProfileRequest;
import com.vn.caro_game.dtos.response.ApiResponse;
import com.vn.caro_game.dtos.response.UserProfileResponse;
import com.vn.caro_game.services.interfaces.IUserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/user-profile")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(
    name = "User Profile Management",
    description = "APIs for managing user profile information, updating user data, and handling avatar uploads"
)
public class UserProfileController {

    IUserProfileService userProfileService;

    /**
     * Retrieves the profile information of the currently authenticated user.
     * 
     * This endpoint returns comprehensive user profile data including user identification,
     * display information, and account metadata. Authentication is required through JWT token.
     *
     * @param userDetails the authenticated user details from security context
     * @return ResponseEntity containing user profile data
     */
    @GetMapping()
    @Operation(
        summary = "Get current user profile",
        description = """
            Retrieves the profile information of the currently authenticated user.
            
            This endpoint returns comprehensive user profile data including:
            - User identification details (ID, username, email)
            - Display information (display name, avatar URL)
            - Account metadata (creation timestamp)
            
            **Authentication Required**: This endpoint requires a valid JWT token in the Authorization header.
            """,
        operationId = UserProfileConstants.GET_USER_PROFILE_OPERATION_ID
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User profile retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                        {
                          "success": true,
                          "message": "User profile retrieved successfully",
                          "data": {
                            "id": 1,
                            "username": "john_doe123",
                            "email": "john@example.com",
                            "displayName": "John Doe",
                            "avatarUrl": "/uploads/avatars/user_1_avatar.jpg",
                            "createdAt": "2024-01-01T10:00:00"
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required - Invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        var result = userProfileService.getUserProfile(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(UserProfileConstants.USER_PROFILE_RETRIEVED_SUCCESS, result));
    }

    /**
     * Updates the profile information of the currently authenticated user.
     * 
     * This endpoint allows users to update their username and display name.
     * All input data is validated before processing and users can only update their own profile.
     *
     * @param userDetails the authenticated user details from security context
     * @param request the profile update request containing new information
     * @return ResponseEntity containing updated user profile data
     */
    @PutMapping()
    @Operation(
        summary = "Update user profile information",
        description = """
            Updates the profile information of the currently authenticated user.
            
            **Updatable Fields:**
            - Username (must be unique)
            - Display name
            - Email (must be unique and valid format)
            
            **Security:** Users can only update their own profile data.
            **Validation:** All input data is validated before processing.
            """,
        operationId = UserProfileConstants.UPDATE_USER_PROFILE_OPERATION_ID
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Profile updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                        {
                          "success": true,
                          "message": "Profile updated successfully",
                          "data": {
                            "id": 1,
                            "username": "john_doe_updated",
                            "email": "john.updated@example.com",
                            "displayName": "John Doe Updated",
                            "avatarUrl": "/uploads/avatars/user_1_avatar.jpg",
                            "createdAt": "2024-01-01T10:00:00"
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Validation error - Invalid input data",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = """
                        {
                          "success": false,
                          "message": "Username already exists",
                          "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
        @Parameter(
            description = "Profile update request containing new information",
            required = true
        )
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        var result = userProfileService.updateProfile(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(UserProfileConstants.PROFILE_UPDATED_SUCCESS, result));
    }

    /**
     * Uploads a new avatar image for the currently authenticated user.
     * 
     * This endpoint handles avatar file upload with validation for file format and size.
     * It automatically replaces existing avatar and generates unique filename to prevent conflicts.
     *
     * @param userDetails the authenticated user details from security context
     * @param avatarFile the avatar image file to upload
     * @return ResponseEntity containing updated user profile data with new avatar URL
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload user avatar",
        description = """
            Uploads a new avatar image for the currently authenticated user.
            
            **File Requirements:**
            - Supported formats: JPEG, PNG, GIF
            - Maximum size: 5MB
            - Recommended dimensions: 200x200 pixels or larger
            
            **Behavior:**
            - Replaces existing avatar if present
            - Automatically deletes old avatar file
            - Generates unique filename to prevent conflicts
            """,
        operationId = UserProfileConstants.UPLOAD_AVATAR_OPERATION_ID
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Avatar uploaded successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                        {
                          "success": true,
                          "message": "Avatar uploaded successfully",
                          "data": {
                            "id": 1,
                            "username": "john_doe123",
                            "email": "john@example.com",
                            "displayName": "John Doe",
                            "avatarUrl": "/uploads/avatars/user_1_avatar_new.jpg",
                            "createdAt": "2024-01-01T10:00:00"
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid file - Wrong format, too large, or corrupted",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "File Validation Error",
                    value = """
                        {
                          "success": false,
                          "message": "File size exceeds maximum limit of 5MB",
                          "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "File upload failed due to server error"
        )
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadAvatar(
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
        @Parameter(
            description = "Avatar image file (JPEG, PNG, GIF, max 5MB)",
            required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        @RequestParam(UserProfileConstants.AVATAR_PARAM_NAME) MultipartFile avatarFile
    ) {
        log.info("Starting avatar upload process for user: {}", userDetails.getUserId());
        var result = userProfileService.updateAvatar(userDetails.getUserId(), avatarFile);
        return ResponseEntity.ok(ApiResponse.success(UserProfileConstants.AVATAR_UPLOADED_SUCCESS, result));
    }

    /**
     * Updates both profile information and avatar in a single request.
     * 
     * This endpoint performs an atomic operation to update both profile data and avatar.
     * Either both operations succeed or both fail to maintain data consistency.
     *
     * @param userDetails the authenticated user details from security context
     * @param request the profile update request containing new information
     * @param avatarFile the optional avatar image file to upload
     * @return ResponseEntity containing updated user profile data
     */
    @PutMapping(value = "/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Update profile and avatar together",
        description = """
            Updates both profile information and avatar in a single request.
            This is useful for complete profile updates from user settings pages.
            
            **Features:**
            - Atomic operation - either both succeed or both fail
            - Optional avatar - can update profile without changing avatar
            - Validates all data before making any changes
            """,
        operationId = UserProfileConstants.UPDATE_COMPLETE_PROFILE_OPERATION_ID
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Profile and avatar updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Validation error in profile data or avatar file"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateCompleteProfile(
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
        @Parameter(description = "Profile update request") @Valid UpdateProfileRequest request,
        @Parameter(description = "Optional avatar file") @RequestParam(value = UserProfileConstants.AVATAR_PARAM_NAME, required = false) MultipartFile avatarFile
    ) {
        log.info("Starting complete profile update for user: {}", userDetails.getUserId());
        var result = userProfileService.updateProfileWithAvatar(userDetails.getUserId(), request, avatarFile);
        return ResponseEntity.ok(ApiResponse.success(UserProfileConstants.PROFILE_UPDATED_SUCCESS, result));
    }
}