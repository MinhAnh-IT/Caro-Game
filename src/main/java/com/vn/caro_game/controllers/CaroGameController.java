package com.vn.caro_game.controllers;

import com.vn.caro_game.configs.CustomUserDetails;
import com.vn.caro_game.dtos.request.GameMoveRequest;
import com.vn.caro_game.dtos.response.ApiResponse;
import com.vn.caro_game.dtos.response.GameMoveResponse;
import com.vn.caro_game.services.interfaces.CaroGameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Caro Game operations.
 * Provides REST API endpoints for game move operations.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Caro Game", description = "APIs for Caro game operations")
public class CaroGameController {

    private final CaroGameService caroGameService;

    @Operation(
        summary = "Make a move in Caro game",
        description = "Makes a move in the Caro game at the specified coordinates. " +
                     "Validates the move, updates game state, checks for win/draw conditions, " +
                     "and broadcasts the result to all players in the room via WebSocket."
    )
    @PostMapping("/{roomId}/moves")
    public ResponseEntity<ApiResponse<GameMoveResponse>> makeMove(
            @Parameter(description = "Room ID where the game is being played", example = "1")
            @PathVariable Long roomId,
            @Parameter(description = "Game move request containing coordinates")
            @Valid @RequestBody GameMoveRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.debug("REST API: Making move for room {} by user {} at position ({}, {})", 
                roomId, userDetails.getUserId(), request.getXPosition(), request.getYPosition());
        
        GameMoveResponse response = caroGameService.makeMove(roomId, request, userDetails.getUserId());
        
        log.debug("REST API: Move made successfully for room {}: {}", roomId, response);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Get current board state",
        description = "Retrieves the current board state for a game room as a 15x15 matrix."
    )
    @GetMapping("/{roomId}/board")
    public ResponseEntity<ApiResponse<int[][]>> getCurrentBoard(
            @Parameter(description = "Room ID to get board state for", example = "1")
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.debug("REST API: Getting board state for room {} by user {}", 
                roomId, userDetails.getUserId());
        
        int[][] board = caroGameService.getCurrentBoard(roomId);
        
        log.debug("REST API: Board state retrieved successfully for room {}", roomId);
        
        return ResponseEntity.ok(ApiResponse.success(board));
    }

    @Operation(
        summary = "Get player symbol",
        description = "Gets the player symbol (X or O) for the authenticated user in a specific room."
    )
    @GetMapping("/{roomId}/player-symbol")
    public ResponseEntity<ApiResponse<String>> getPlayerSymbol(
            @Parameter(description = "Room ID to get player symbol for", example = "1")
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.debug("REST API: Getting player symbol for room {} by user {}", 
                roomId, userDetails.getUserId());
        
        String symbol = caroGameService.getPlayerSymbol(roomId, userDetails.getUserId());
        
        log.debug("REST API: Player symbol retrieved successfully for room {}: {}", roomId, symbol);
        
        return ResponseEntity.ok(ApiResponse.success("Player symbol retrieved", symbol));
    }
}
