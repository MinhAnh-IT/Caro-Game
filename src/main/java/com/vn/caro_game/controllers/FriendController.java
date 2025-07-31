package com.vn.caro_game.controllers;

import com.vn.caro_game.configs.CustomUserDetails;
import com.vn.caro_game.constants.FriendConstants;
import com.vn.caro_game.dtos.response.ApiResponse;
import com.vn.caro_game.dtos.response.ErrorResponse;
import com.vn.caro_game.dtos.FriendRequestDto;
import com.vn.caro_game.dtos.FriendResponseDto;
import com.vn.caro_game.dtos.UserSearchResponseDto;
import com.vn.caro_game.services.interfaces.IFriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(FriendConstants.FRIEND_BASE_PATH)
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Friend Management", description = "APIs for managing friendships and friend requests")
@SecurityRequirement(name = "bearerAuth")
public class FriendController {

    private final IFriendService friendService;

    @Operation(
        summary = "Search users by display name or username",
        description = "Search for users to send friend requests. Returns users matching the search term in display name or username. Search is case-insensitive and supports partial matching."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Users found successfully",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": true,
                      "statusCode": 200,
                      "message": "Users found successfully",
                      "data": [
                        {
                          "id": 2,
                          "username": "john_doe",
                          "displayName": "John Doe",
                          "avatarUrl": "https://example.com/avatars/john.jpg",
                          "relationshipStatus": "none",
                          "canSendRequest": true
                        },
                        {
                          "id": 3,
                          "username": "jane_smith",
                          "displayName": "Jane Smith",
                          "avatarUrl": "https://example.com/avatars/jane.jpg",
                          "relationshipStatus": "friends",
                          "canSendRequest": false
                        }
                      ],
                      "errorCode": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 400,
                      "message": "Search term is required",
                      "data": null,
                      "errorCode": "INVALID_REQUEST_DATA"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid token",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 401,
                      "message": "Unauthorized access",
                      "data": null,
                      "errorCode": "UNAUTHORIZED"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 500,
                      "message": "Internal server error occurred",
                      "data": null,
                      "errorCode": "INTERNAL_SERVER_ERROR"
                    }
                    """
                )
            )
        )
    })
    @PostMapping(FriendConstants.SEARCH_PATH)
    public ResponseEntity<ApiResponse<List<UserSearchResponseDto>>> searchUsers(
            @Valid @RequestBody FriendRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = userDetails.getUserId();
        List<UserSearchResponseDto> users = friendService.searchUsers(request.getSearchTerm(), currentUserId);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @Operation(
        summary = "Send friend request",
        description = "Send a friend request to another user by their user ID."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Friend request sent successfully",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": true,
                      "statusCode": 200,
                      "message": "Friend request sent successfully",
                      "data": null,
                      "errorCode": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad request - Cannot add yourself or already have relationship",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 400,
                      "message": "Cannot send friend request to yourself",
                      "data": null,
                      "errorCode": "CANNOT_ADD_YOURSELF"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid token",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 401,
                      "message": "Unauthorized access",
                      "data": null,
                      "errorCode": "UNAUTHORIZED"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 404,
                      "message": "User not found",
                      "data": null,
                      "errorCode": "USER_NOT_FOUND"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Friend request already sent",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 409,
                      "message": "Friend request already sent or users are already friends",
                      "data": null,
                      "errorCode": "FRIEND_REQUEST_ALREADY_SENT"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 500,
                      "message": "Internal server error occurred",
                      "data": null,
                      "errorCode": "INTERNAL_SERVER_ERROR"
                    }
                    """
                )
            )
        )
    })
    @PostMapping(FriendConstants.REQUEST_PATH)
    public ResponseEntity<ApiResponse<Void>> sendFriendRequest(
            @Parameter(description = "ID of the user to send friend request to", example = "123")
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = userDetails.getUserId();
        friendService.sendFriendRequest(currentUserId, userId);

        return ResponseEntity.ok(ApiResponse.success(FriendConstants.FRIEND_REQUEST_SENT_SUCCESS));
    }

    @Operation(
        summary = "Accept friend request",
        description = "Accept a pending friend request from another user. Creates bidirectional friendship."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Friend request accepted successfully",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": true,
                      "statusCode": 200,
                      "message": "Friend request accepted successfully",
                      "data": null,
                      "errorCode": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Friend request already responded or invalid status",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 400,
                      "message": "Friend request has already been responded to",
                      "data": null,
                      "errorCode": "FRIEND_REQUEST_ALREADY_RESPONDED"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid token",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 401,
                      "message": "Unauthorized access",
                      "data": null,
                      "errorCode": "UNAUTHORIZED"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Friend request not found",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 404,
                      "message": "Friend request not found",
                      "data": null,
                      "errorCode": "FRIEND_REQUEST_NOT_FOUND"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 500,
                      "message": "Internal server error occurred",
                      "data": null,
                      "errorCode": "INTERNAL_SERVER_ERROR"
                    }
                    """
                )
            )
        )
    })
    @PostMapping(FriendConstants.ACCEPT_PATH)
    public ResponseEntity<ApiResponse<Void>> acceptFriendRequest(
            @Parameter(description = "ID of the user who sent the friend request", example = "123")
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = userDetails.getUserId();
        friendService.acceptFriendRequest(currentUserId, userId);

        return ResponseEntity.ok(ApiResponse.success(FriendConstants.FRIEND_REQUEST_ACCEPTED_SUCCESS));
    }

    @Operation(
        summary = "Reject friend request",
        description = "Reject a pending friend request from another user."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Friend request rejected successfully",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": true,
                      "statusCode": 200,
                      "message": "Friend request rejected successfully",
                      "data": null,
                      "errorCode": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Friend request already responded or invalid status",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 400,
                      "message": "Friend request has already been responded to",
                      "data": null,
                      "errorCode": "FRIEND_REQUEST_ALREADY_RESPONDED"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid token",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 401,
                      "message": "Unauthorized access",
                      "data": null,
                      "errorCode": "UNAUTHORIZED"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Friend request not found",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 404,
                      "message": "Friend request not found",
                      "data": null,
                      "errorCode": "FRIEND_REQUEST_NOT_FOUND"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 500,
                      "message": "Internal server error occurred",
                      "data": null,
                      "errorCode": "INTERNAL_SERVER_ERROR"
                    }
                    """
                )
            )
        )
    })
    @PostMapping(FriendConstants.REJECT_PATH)
    public ResponseEntity<ApiResponse<Void>> rejectFriendRequest(
            @Parameter(description = "ID of the user who sent the friend request", example = "123")
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = userDetails.getUserId();
        friendService.rejectFriendRequest(currentUserId, userId);

        return ResponseEntity.ok(ApiResponse.success(FriendConstants.FRIEND_REQUEST_REJECTED_SUCCESS));
    }

    @Operation(
        summary = "Get friends list",
        description = "Retrieve the list of accepted friends for the current user."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Friends list retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": true,
                      "statusCode": 200,
                      "message": "Friends list retrieved successfully",
                      "data": [
                        {
                          "userId": 2,
                          "username": "john_doe",
                          "displayName": "John Doe",
                          "avatarUrl": "https://example.com/avatars/john.jpg",
                          "status": "ACCEPTED",
                          "createdAt": "2025-07-30T10:15:30",
                          "isOnline": true
                        },
                        {
                          "userId": 3,
                          "username": "jane_smith",
                          "displayName": "Jane Smith",
                          "avatarUrl": "https://example.com/avatars/jane.jpg",
                          "status": "ACCEPTED",
                          "createdAt": "2025-07-29T14:20:15",
                          "isOnline": false
                        }
                      ],
                      "errorCode": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid token",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 401,
                      "message": "Unauthorized access",
                      "data": null,
                      "errorCode": "UNAUTHORIZED"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 500,
                      "message": "Internal server error occurred",
                      "data": null,
                      "errorCode": "INTERNAL_SERVER_ERROR"
                    }
                    """
                )
            )
        )
    })
    @GetMapping(FriendConstants.LIST_PATH)
    public ResponseEntity<ApiResponse<List<FriendResponseDto>>> getFriendsList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = userDetails.getUserId();
        List<FriendResponseDto> friends = friendService.getFriendsList(currentUserId);

        return ResponseEntity.ok(ApiResponse.success(friends));
    }

    @Operation(
        summary = "Get pending friend requests (received)",
        description = "Retrieve the list of pending friend requests received by the current user."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pending friend requests retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": true,
                      "statusCode": 200,
                      "message": "Pending friend requests retrieved successfully",
                      "data": [
                        {
                          "userId": 4,
                          "username": "alice_brown",
                          "displayName": "Alice Brown",
                          "avatarUrl": "https://example.com/avatars/alice.jpg",
                          "status": "PENDING",
                          "createdAt": "2025-07-31T08:30:45",
                          "isOnline": true
                        },
                        {
                          "userId": 5,
                          "username": "bob_wilson",
                          "displayName": "Bob Wilson",
                          "avatarUrl": "https://example.com/avatars/bob.jpg",
                          "status": "PENDING",
                          "createdAt": "2025-07-30T16:45:20",
                          "isOnline": false
                        }
                      ],
                      "errorCode": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid token",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 401,
                      "message": "Unauthorized access",
                      "data": null,
                      "errorCode": "UNAUTHORIZED"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 500,
                      "message": "Internal server error occurred",
                      "data": null,
                      "errorCode": "INTERNAL_SERVER_ERROR"
                    }
                    """
                )
            )
        )
    })
    @GetMapping(FriendConstants.RECEIVED_REQUESTS_PATH)
    public ResponseEntity<ApiResponse<List<FriendResponseDto>>> getPendingFriendRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = userDetails.getUserId();
        List<FriendResponseDto> requests = friendService.getPendingFriendRequests(currentUserId);

        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @Operation(
        summary = "Get sent friend requests",
        description = "Retrieve the list of friend requests sent by the current user that are still pending."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Sent friend requests retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": true,
                      "statusCode": 200,
                      "message": "Sent friend requests retrieved successfully",
                      "data": [
                        {
                          "userId": 6,
                          "username": "charlie_davis",
                          "displayName": "Charlie Davis",
                          "avatarUrl": "https://example.com/avatars/charlie.jpg",
                          "status": "PENDING",
                          "createdAt": "2025-07-31T09:15:30",
                          "isOnline": false
                        },
                        {
                          "userId": 7,
                          "username": "diana_evans",
                          "displayName": "Diana Evans",
                          "avatarUrl": "https://example.com/avatars/diana.jpg",
                          "status": "PENDING",
                          "createdAt": "2025-07-30T20:40:15",
                          "isOnline": true
                        }
                      ],
                      "errorCode": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid token",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 401,
                      "message": "Unauthorized access",
                      "data": null,
                      "errorCode": "UNAUTHORIZED"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "statusCode": 500,
                      "message": "Internal server error occurred",
                      "data": null,
                      "errorCode": "INTERNAL_SERVER_ERROR"
                    }
                    """
                )
            )
        )
    })
    @GetMapping(FriendConstants.SENT_REQUESTS_PATH)
    public ResponseEntity<ApiResponse<List<FriendResponseDto>>> getSentFriendRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = userDetails.getUserId();
        List<FriendResponseDto> requests = friendService.getSentFriendRequests(currentUserId);

        return ResponseEntity.ok(ApiResponse.success(requests));
    }
}
