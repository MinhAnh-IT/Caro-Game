package com.vn.caro_game.services.interfaces;

import com.vn.caro_game.dtos.request.GameMoveRequest;
import com.vn.caro_game.dtos.response.GameMoveResponse;

/**
 * Service interface for Caro Game operations.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
public interface CaroGameService {

    /**
     * Makes a move in the Caro game.
     * Validates the move, updates the game state, checks for win/draw conditions,
     * and broadcasts the result to all players in the room.
     * 
     * @param roomId the room ID where the game is being played
     * @param request the move request containing coordinates
     * @param userId the ID of the user making the move
     * @return the game move response with updated game state
     */
    GameMoveResponse makeMove(Long roomId, GameMoveRequest request, Long userId);

    /**
     * Gets the current board state for a game room.
     * 
     * @param roomId the room ID to get board state for
     * @return the current board as 15x15 matrix
     */
    int[][] getCurrentBoard(Long roomId);

    /**
     * Checks if a move is valid at the given position.
     * 
     * @param board the current board state
     * @param xPosition x coordinate (0-14)
     * @param yPosition y coordinate (0-14)
     * @return true if the move is valid, false otherwise
     */
    boolean isValidMove(int[][] board, int xPosition, int yPosition);

    /**
     * Checks if a player has won the game after making a move.
     * 
     * @param board the current board state
     * @param xPosition x coordinate of the last move
     * @param yPosition y coordinate of the last move
     * @param playerValue the player value (1 for X, 2 for O)
     * @return true if the player has won, false otherwise
     */
    boolean checkWin(int[][] board, int xPosition, int yPosition, int playerValue);

    /**
     * Checks if the game is a draw (board is full with no winner).
     * 
     * @param board the current board state
     * @return true if the game is a draw, false otherwise
     */
    boolean checkDraw(int[][] board);

    /**
     * Initializes a new empty board for a game.
     * 
     * @return a new 15x15 empty board
     */
    int[][] initializeBoard();

    /**
     * Gets the player value for a user in a specific room.
     * 
     * @param roomId the room ID
     * @param userId the user ID
     * @return 1 for player X, 2 for player O, 0 if not a player
     */
    int getPlayerValue(Long roomId, Long userId);

    /**
     * Gets the player symbol for a user in a specific room.
     * 
     * @param roomId the room ID
     * @param userId the user ID
     * @return "X" for player X, "O" for player O, empty string if not a player
     */
    String getPlayerSymbol(Long roomId, Long userId);
}
