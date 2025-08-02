package com.vn.caro_game.controllers;

import com.vn.caro_game.controllers.base.BaseController;
import com.vn.caro_game.configs.CustomUserDetails;
import com.vn.caro_game.services.interfaces.GameRoomService;
import com.vn.caro_game.integrations.redis.RedisService;
import com.vn.caro_game.repositories.GameRoomRepository;
import com.vn.caro_game.repositories.RoomPlayerRepository;
import com.vn.caro_game.entities.GameRoom;
import com.vn.caro_game.entities.RoomPlayer;
import com.vn.caro_game.mappers.GameRoomMapper;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.enums.StatusCode;
import com.vn.caro_game.dtos.request.*;
import com.vn.caro_game.dtos.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;



/**
 * REST Controller for Game Room operations.
 * Handles room creation, joining, leaving, and chat functionality.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Tag(name = "Game Room", description = "Game Room management APIs")
@RestController
@RequestMapping("/api/rooms")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class GameRoomController extends BaseController {

    private final GameRoomService gameRoomService;
    private final GameRoomRepository gameRoomRepository;
    private final RoomPlayerRepository roomPlayerRepository;
    private final GameRoomMapper gameRoomMapper;
    private final RedisService redisService;

    /**
     * Creates a new game room with specified type and settings.
     */
    @Operation(summary = "Create a new game room", description = "Create a new game room with specified type and settings")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Room created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid room data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4026", description = "User already in another room"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<GameRoomResponse>> createRoom(
            @Valid @RequestBody CreateRoomRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        GameRoomResponse response = gameRoomService.createRoom(request, userDetails.getUserId());
        return created(response, "Room created successfully");
    }



    /**
     * Finds or creates a public room for quick play matchmaking.
     */
    @Operation(summary = "Quick play", description = "Find or create a public room for quick play")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Room ready for quick play"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4026", description = "User already in another room"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/quick-play")
    public ResponseEntity<ApiResponse<GameRoomResponse>> quickPlay(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        GameRoomResponse response = gameRoomService.findOrCreatePublicRoom(userDetails.getUserId());
        return success(response, "Room ready for quick play");
    }





    /**
     * Gets detailed information about a specific room.
     */
    @Operation(summary = "Get room details", description = "Get detailed information about a specific room")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Room details retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4017", description = "Room not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<GameRoomResponse>> getRoomDetails(
            @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        GameRoomResponse response = gameRoomService.getRoomDetails(roomId, userDetails.getUserId());
        return success(response, "Room details retrieved successfully");
    }

    /**
     * Gets a paginated list of available public rooms.
     */
    @Operation(summary = "Get public rooms", description = "Get a paginated list of available public rooms")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Public rooms retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Page<PublicRoomResponse>>> getPublicRooms(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<PublicRoomResponse> response = gameRoomService.getPublicRooms(pageable);
            return success(response, "Public rooms retrieved successfully");
        } catch (org.springframework.dao.InvalidDataAccessApiUsageException e) {
            // If sort parameter is invalid, use default sort
            Pageable defaultPageable = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(), 
                Sort.by(Sort.Direction.DESC, "createdAt")
            );
            Page<PublicRoomResponse> response = gameRoomService.getPublicRooms(defaultPageable);
            return success(response, "Public rooms retrieved successfully");
        }
    }

    /**
     * Gets a paginated list of rooms the user has participated in.
     */
    @Operation(summary = "Get user's rooms", description = "Get a paginated list of rooms the user has participated in")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User rooms retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user-rooms")
    public ResponseEntity<ApiResponse<Page<GameRoomResponse>>> getUserRooms(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Page<GameRoomResponse> response = gameRoomService.getUserRooms(userDetails.getUserId(), pageable);
            return success(response, "User rooms retrieved successfully");
        } catch (org.springframework.dao.InvalidDataAccessApiUsageException e) {
            // If sort parameter is invalid, use default sort
            Pageable defaultPageable = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(), 
                Sort.by(Sort.Direction.DESC, "createdAt")
            );
            Page<GameRoomResponse> response = gameRoomService.getUserRooms(userDetails.getUserId(), defaultPageable);
            return success(response, "User rooms retrieved successfully");
        }
    }

    /**
     * Gets the current room the user is in.
     */
    @Operation(summary = "Get current room", description = "Get the current room the user is in")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Current room retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4020", description = "User not in any room"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<GameRoomResponse>> getCurrentRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        GameRoomResponse response = gameRoomService.getCurrentUserRoom(userDetails.getUserId());
        return success(response, "Current room retrieved successfully");
    }

    /**
     * Sends an invitation to a friend to join the current room.
     */
    @Operation(summary = "Invite friend to room", description = "Send an invitation to a friend to join the current room")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invitation sent successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot send invitation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room or friend not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4017", description = "Room not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4018", description = "Room is full"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4021", description = "User not in room"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{roomId}/invite")
    public ResponseEntity<ApiResponse<Void>> inviteFriend(
            @PathVariable Long roomId,
            @Valid @RequestBody InviteFriendRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        gameRoomService.inviteFriend(roomId, request, userDetails.getUserId());
        return success("Invitation sent successfully");
    }

    /**
     * Gets the game history for the current user.
     */
    @Operation(summary = "Get user game history", description = "Get paginated game history for the current user")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Game history retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<GameHistoryResponse>>> getUserGameHistory(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Page<GameHistoryResponse> response = gameRoomService.getUserGameHistory(userDetails.getUserId(), pageable);
            return success(response, "Game history retrieved successfully");
        } catch (org.springframework.dao.InvalidDataAccessApiUsageException e) {
            // If sort parameter is invalid, use default sort
            Pageable defaultPageable = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(), 
                Sort.by(Sort.Direction.DESC, "createdAt")
            );
            Page<GameHistoryResponse> response = gameRoomService.getUserGameHistory(userDetails.getUserId(), defaultPageable);
            return success(response, "Game history retrieved successfully");
        }
    }

    /**
     * Joins a room using a join code (for private rooms).
     */
    @Operation(summary = "Join room by code", description = "Join a private room using its unique join code")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Joined room successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid join code"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4017", description = "Room not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4018", description = "Room is full"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4026", description = "User already in another room"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/join-by-code")
    public ResponseEntity<ApiResponse<GameRoomResponse>> joinRoomByCode(
            @Valid @RequestBody JoinRoomRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        GameRoomResponse response = gameRoomService.joinRoomByCode(request, userDetails.getUserId());
        return success(response, "Joined room successfully");
    }

    /**
     * Joins a room by room ID (for public rooms).
     */
    @Operation(summary = "Join room by ID", description = "Join a public room using its room ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Joined room successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot join room"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4017", description = "Room not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4018", description = "Room is full"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4026", description = "User already in another room"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponse<GameRoomResponse>> joinRoomById(
            @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        GameRoomResponse response = gameRoomService.joinRoom(roomId, userDetails.getUserId());
        return success(response, "Joined room successfully");
    }

    /**
     * Leaves the current room.
     */
    @Operation(summary = "Leave room", description = "Leave the current room")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Left room successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4017", description = "Room not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4021", description = "User not in room"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        gameRoomService.leaveRoom(roomId, userDetails.getUserId());
        return success("Left room successfully");
    }

    /**
     * Finds a room by its join code (for private room discovery).
     */
    @Operation(summary = "Find room by code", description = "Find room information using join code (for private room discovery)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Room found successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/find-by-code/{joinCode}")
    public ResponseEntity<ApiResponse<GameRoomResponse>> findRoomByCode(
            @PathVariable String joinCode,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Find room by join code without joining
        GameRoom room = gameRoomRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new CustomException(StatusCode.INVALID_JOIN_CODE));
        
        // Build room response for discovery
        List<RoomPlayerResponse> players = buildRoomPlayerResponses(room.getId());
        GameRoomResponse response = gameRoomMapper.mapToGameRoomResponse(room, players);
        
        return success(response, "Room found successfully");
    }

    /**
     * Creates a rematch for a finished game room (legacy API).
     */
    @Operation(summary = "Create rematch", description = "Create a new room for rematch with the same players")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rematch room created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot create rematch"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4017", description = "Room not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4021", description = "User not in room"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "4024", description = "Game not finished"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{roomId}/rematch")
    public ResponseEntity<ApiResponse<GameRoomResponse>> createRematch(
            @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        GameRoomResponse response = gameRoomService.createRematch(roomId, userDetails.getUserId());
        return success(response, "Rematch room created successfully");
    }

    private List<RoomPlayerResponse> buildRoomPlayerResponses(Long roomId) {
        // This would normally be in the service layer, but adding here for the find endpoint
        List<RoomPlayer> roomPlayers = roomPlayerRepository.findByRoom_Id(roomId);
        
        return roomPlayers.stream()
                .map(roomPlayer -> {
                    boolean isOnline = redisService.isUserOnline(roomPlayer.getUser().getId());
                    return gameRoomMapper.mapToRoomPlayerResponse(roomPlayer, isOnline);
                })
                .collect(Collectors.toList());
    }

}
