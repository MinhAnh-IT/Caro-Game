package com.vn.caro_game.enums;

/**
 * Enum representing the result of a game match
 */
public enum GameResult {
    /**
     * Player X wins the game
     */
    X_WIN,
    
    /**
     * Player O wins the game
     */
    O_WIN,
    
    /**
     * Game ends in a draw
     */
    DRAW,
    
    /**
     * Game is still ongoing
     */
    ONGOING,
    
    /**
     * Player won the game (for RoomPlayer)
     */
    WIN,
    
    /**
     * Player lost the game (for RoomPlayer)
     */
    LOSE,
    
    /**
     * No result yet (for RoomPlayer)
     */
    NONE
}
