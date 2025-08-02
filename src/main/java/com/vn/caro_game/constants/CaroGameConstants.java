package com.vn.caro_game.constants;

/**
 * Constants for Caro Game functionality.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
public final class CaroGameConstants {
    
    private CaroGameConstants() {
        // Utility class
    }
    
    // Board configuration
    public static final int BOARD_SIZE = 15;
    public static final int WIN_COUNT = 5;
    
    // Player symbols
    public static final String PLAYER_X_SYMBOL = "X";
    public static final String PLAYER_O_SYMBOL = "O";
    
    // Board values
    public static final int EMPTY_CELL = 0;
    public static final int PLAYER_X_VALUE = 1;
    public static final int PLAYER_O_VALUE = 2;
    
    // Game move directions for win checking
    public static final int[][] WIN_DIRECTIONS = {
        {0, 1},   // Horizontal
        {1, 0},   // Vertical  
        {1, 1},   // Diagonal main
        {1, -1}   // Diagonal anti
    };
    
    // WebSocket topics for game moves
    public static final String TOPIC_GAME_MOVE = "/topic/game/%d/move";
    public static final String TOPIC_GAME_END = "/topic/game/%d/end";
    
    // Game messages
    public static final String MSG_INVALID_MOVE = "Invalid move";
    public static final String MSG_NOT_YOUR_TURN = "Not your turn";
    public static final String MSG_GAME_NOT_ACTIVE = "Game is not active";
    public static final String MSG_PLAYER_NOT_IN_ROOM = "Player is not in this room";
    public static final String MSG_GAME_WON = "Game won";
    public static final String MSG_GAME_DRAW = "Game ended in draw";
}
