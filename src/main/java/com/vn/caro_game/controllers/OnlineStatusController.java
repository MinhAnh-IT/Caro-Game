package com.vn.caro_game.controllers;

import com.vn.caro_game.configs.CustomUserDetails;
import com.vn.caro_game.dtos.response.ApiResponse;
import com.vn.caro_game.dtos.response.FriendOnlineStatusResponse;
import com.vn.caro_game.integrations.redis.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for managing user online status.
 * 
 * <p>This controller provides endpoints to check the online status of users,
 * specifically focusing on retrieving the online status of current user's friends.
 * Online status is managed through Redis with TTL-based keys.</p>
 * 
 * <p><strong>Security:</strong> All endpoints require JWT authentication.
 * Users can only access their own friends' online status.</p>
 * 
 * @author Caro Game Team
 * @since 1.0.0
 * @see RedisService
 */
@RestController
@RequestMapping("/api/online-status")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Online Status", description = "APIs for managing and checking user online status")
public class OnlineStatusController {
    RedisService redisService;

    @Operation(
        summary = "Get Online Status of Current User's Friends",
        description = """
            Retrieves the detailed information and online status of all friends for the current authenticated user.
            
            **Features:**
            - Returns comprehensive friend information including userId, displayName, avatarUrl, and online status
            - Uses Redis TTL-based online tracking
            - Efficient batch checking of multiple users
            - Only includes friends with 'ACCEPTED' status
            - Requires JWT authentication
            
            **Online Status Logic:**
            - `true`: Friend has an active session (Redis key exists with valid TTL)
            - `false`: Friend is offline or session has expired
            
            **Response Format:**
            ```json
            {
              "success": true,
              "statusCode": 200,
              "message": "Friends online status retrieved successfully",
              "data": [
                {
                  "userId": 123,
                  "displayName": "John Doe",
                  "avatarUrl": "https://example.com/avatar1.jpg",
                  "status": true
                },
                {
                  "userId": 456,
                  "displayName": "Jane Smith",
                  "avatarUrl": "https://example.com/avatar2.jpg",
                  "status": false
                }
              ]
            }
            ```
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved friends' online status",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class)
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Authentication required",
                content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    @GetMapping("/friends")
    public ResponseEntity<ApiResponse<List<FriendOnlineStatusResponse>>> getFriendsOnlineStatus(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
            ) {
        // Get friends online status with detailed information
        List<FriendOnlineStatusResponse> friendsList = redisService.getFriendsOnlineStatus(customUserDetails.getUserId());

        ApiResponse<List<FriendOnlineStatusResponse>> response = ApiResponse.success(
            "Friends online status retrieved successfully", friendsList
        );
        
        return ResponseEntity.ok(response);
    }
}
