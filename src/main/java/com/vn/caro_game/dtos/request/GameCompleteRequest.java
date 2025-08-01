package com.vn.caro_game.dtos.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for completing a game
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameCompleteRequest {
    
    @NotNull(message = "Winner ID is required")
    Long winnerId;
    
    @NotNull(message = "Loser ID is required")
    Long loserId;
    
    String gameData; // Optional game board data or moves
}
