package com.vn.caro_game.controllers;

import com.vn.caro_game.integrations.redis.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller for managing user online status.
 * 
 * <p>This controller provides endpoints to check the online status of users,
 * specifically focusing on retrieving the online status of a user's friends.
 * Online status is managed through Redis with TTL-based keys.</p>
 * 
 * @author Caro Game Team
 * @since 1.0.0
 * @see RedisService
 */
@RestController
@RequestMapping("/api/online-status")
@RequiredArgsConstructor
@Tag(name = "Online Status", description = "APIs for managing and checking user online status")
public class OnlineStatusController {
    private final RedisService redisService;

    @Operation(
        summary = "Get Online Status of User's Friends",
        description = """
            Retrieves the online status of all friends for a specific user.
            
            **Features:**
            - Returns online status (true/false) for all accepted friends
            - Uses Redis TTL-based online tracking
            - Efficient batch checking of multiple users
            - Only includes friends with 'ACCEPTED' status
            
            **Online Status Logic:**
            - `true`: Friend has an active session (Redis key exists with valid TTL)
            - `false`: Friend is offline or session has expired
            
            **Response Format:**
            ```json
            {
              "123": true,   // Friend ID 123 is online
              "456": false,  // Friend ID 456 is offline
              "789": true    // Friend ID 789 is online
            }
            ```
            """,
        parameters = {
            @Parameter(
                name = "userId", 
                description = "The ID of the user whose friends' online status to retrieve",
                example = "1",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved friends' online status",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                        type = "object",
                        example = "{\"123\":true,\"456\":false,\"789\":true}",
                        description = "Map of friend ID to online status (boolean)"
                    )
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    @GetMapping("/friends/{userId}")
    public ResponseEntity<Map<Long, Boolean>> getFriendsOnlineStatus(
            @PathVariable("userId") Long userId) {
        Map<Long, Boolean> friendsStatus = redisService.getFriendsOnlineStatus(userId);
        return ResponseEntity.ok(friendsStatus);
    }
}
