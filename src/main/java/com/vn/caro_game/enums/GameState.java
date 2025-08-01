package com.vn.caro_game.enums;

/**
 * Enum representing the game state with ready system
 */
public enum GameState {
    /**
     * Room is waiting for players to join (< 2 players)
     */
    WAITING_FOR_PLAYERS,
    
    /**
     * 2 players joined, waiting for both to be ready
     */
    WAITING_FOR_READY,
    
    /**
     * Both players ready, about to start game
     */
    READY_TO_START,
    
    /**
     * Game is currently in progress
     */
    IN_PROGRESS,
    
    /**
     * Game ended normally (checkmate/moves completed)
     */
    FINISHED,
    
    /**
     * Game ended because someone surrendered
     */
    ENDED_BY_SURRENDER,
    
    /**
     * Game ended because someone left the room
     */
    ENDED_BY_LEAVE
}
