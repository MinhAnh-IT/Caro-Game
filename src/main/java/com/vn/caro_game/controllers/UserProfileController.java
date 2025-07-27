package com.vn.caro_game.controllers;

import com.vn.caro_game.dtos.request.UpdateProfileRequest;
import com.vn.caro_game.dtos.response.ApiResponse;
import com.vn.caro_game.dtos.response.UserProfileResponse;
import com.vn.caro_game.integrations.jwt.JwtService;
import com.vn.caro_game.services.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for user profile management operations.
 *
 * <p>This controller provides endpoints for managing user profile information
 * including profile retrieval, updates, and avatar management. All endpoints
 * require JWT authentication.</p>
 *
 * <h3>Available Endpoints:</h3>
 * <ul>
 *   <li><strong>GET /api/profile</strong> - Get current user profile</li>
 *   <li><strong>PUT /api/profile</strong> - Update user profile information</li>
 *   <li><strong>POST /api/profile/avatar</strong> - Update user avatar</li>
 *   <li><strong>PUT /api/profile/complete</strong> - Update profile with avatar</li>
 * </ul>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Profile", description = "User profile management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final JwtService jwtService;

    /**
     * Retrieves the current user's profile information.
     *
     * @param request HTTP request to extract JWT token
     * @return ResponseEntity containing user profile information
     */
    @GetMapping
    @Operation(
        summary = "Get user profile",
        description = "Retrieves the current authenticated user's profile information"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(HttpServletRequest request) {
        log.info("=== GET USER PROFILE REQUEST ===");

        Long userId = getUserIdFromToken(request);
        UserProfileResponse profile = userProfileService.getUserProfile(userId);

        log.info("=== USER PROFILE RETRIEVED SUCCESSFULLY FOR USER: {} ===", userId);

        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    /**
     * Updates the current user's profile information.
     *
     * @param request HTTP request to extract JWT token
     * @param updateRequest the profile update request
     * @return ResponseEntity containing updated user profile information
     */
    @PutMapping
    @Operation(
        summary = "Update user profile",
        description = "Updates the current authenticated user's profile information"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data or username already exists"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody UpdateProfileRequest updateRequest) {

        log.info("=== UPDATE PROFILE REQUEST: {} ===", updateRequest);

        Long userId = getUserIdFromToken(request);
        UserProfileResponse updatedProfile = userProfileService.updateProfile(userId, updateRequest);

        log.info("=== PROFILE UPDATED SUCCESSFULLY FOR USER: {} ===", userId);

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedProfile));
    }

    /**
     * Updates the current user's avatar.
     *
     * @param request HTTP request to extract JWT token
     * @param avatarFile the avatar image file to upload
     * @return ResponseEntity containing updated user profile information
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Update user avatar",
        description = "Updates the current authenticated user's avatar image"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Avatar updated successfully",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid file format or size"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateAvatar(
            HttpServletRequest request,
            @Parameter(description = "Avatar image file (JPEG, PNG, GIF, WebP)")
            @RequestParam("avatar") MultipartFile avatarFile) {

        // Check if file is provided
        if (avatarFile == null || avatarFile.isEmpty()) {
            log.warn("=== NO FILE PROVIDED FOR AVATAR UPDATE ===");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Avatar file is required"));
        }

        log.info("=== UPDATE AVATAR REQUEST FOR FILE: {} ===", avatarFile.getOriginalFilename());

        Long userId = getUserIdFromToken(request);
        UserProfileResponse updatedProfile = userProfileService.updateAvatar(userId, avatarFile);

        log.info("=== AVATAR UPDATED SUCCESSFULLY FOR USER: {} ===", userId);

        return ResponseEntity.ok(ApiResponse.success("Avatar updated successfully", updatedProfile));
    }

    /**
     * Updates both profile information and avatar in a single request.
     *
     * @param request HTTP request to extract JWT token
     * @param username the new username
     * @param displayName the new display name
     * @param avatarFile the avatar image file to upload (optional)
     * @return ResponseEntity containing updated user profile information
     */
    @PutMapping(value = "/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Update profile with avatar",
        description = "Updates both profile information and avatar in a single request"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Profile and avatar updated successfully",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data, username already exists, or invalid file"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfileComplete(
            HttpServletRequest request,
            @Parameter(description = "Username (3-20 characters, alphanumeric and underscore only)")
            @RequestParam("username") String username,
            @Parameter(description = "Display name (max 50 characters)")
            @RequestParam(value = "displayName", required = false) String displayName,
            @Parameter(description = "Avatar image file (JPEG, PNG, GIF, WebP)")
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile) {

        log.info("=== UPDATE COMPLETE PROFILE REQUEST: username={}, displayName={}, avatar={} ===",
                username, displayName, avatarFile != null ? avatarFile.getOriginalFilename() : "none");

        // Create update request from form parameters
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setUsername(username);
        updateRequest.setDisplayName(displayName);

        Long userId = getUserIdFromToken(request);
        UserProfileResponse updatedProfile = userProfileService.updateProfileWithAvatar(userId, updateRequest, avatarFile);

        log.info("=== COMPLETE PROFILE UPDATED SUCCESSFULLY FOR USER: {} ===", userId);

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedProfile));
    }

    /**
     * Extracts user ID from JWT token in the request.
     *
     * @param request the HTTP request containing JWT token
     * @return the user ID from the token
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtService.getUserIdFromToken(token);
        }
        throw new RuntimeException("No valid JWT token found");
    }
}
