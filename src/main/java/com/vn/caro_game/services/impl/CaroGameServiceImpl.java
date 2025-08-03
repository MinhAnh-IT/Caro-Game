package com.vn.caro_game.services.impl;

import com.vn.caro_game.constants.CaroGameConstants;
import com.vn.caro_game.dtos.request.GameMoveRequest;
import com.vn.caro_game.dtos.response.GameMoveResponse;
import com.vn.caro_game.entities.GameMatch;
import com.vn.caro_game.entities.GameRoom;
import com.vn.caro_game.entities.Move;
import com.vn.caro_game.entities.RoomPlayer;
import com.vn.caro_game.entities.GameHistory;
import com.vn.caro_game.enums.GameResult;
import com.vn.caro_game.enums.GameState;
import com.vn.caro_game.enums.StatusCode;
import com.vn.caro_game.enums.GameEndReason;
import com.vn.caro_game.enums.PlayerReadyState;
import com.vn.caro_game.enums.RoomStatus;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.repositories.GameMatchRepository;
import com.vn.caro_game.repositories.GameRoomRepository;
import com.vn.caro_game.repositories.MoveRepository;
import com.vn.caro_game.repositories.UserRepository;
import com.vn.caro_game.repositories.GameHistoryRepository;
import com.vn.caro_game.repositories.RoomPlayerRepository;
import com.vn.caro_game.services.interfaces.CaroGameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementation of CaroGameService for handling Caro game logic.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CaroGameServiceImpl implements CaroGameService {

    private final GameRoomRepository gameRoomRepository;
    private final GameMatchRepository gameMatchRepository;
    private final MoveRepository moveRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameHistoryRepository gameHistoryRepository;
    private final RoomPlayerRepository roomPlayerRepository;

    @Override
    @Transactional
    public GameMoveResponse makeMove(Long roomId, GameMoveRequest request, Long userId) {
        // Add null check and logging for debugging
        log.debug("Received move request: roomId={}, userId={}, request={}", roomId, userId, request);
        
        if (request == null) {
            throw new CustomException("Move request cannot be null", StatusCode.INVALID_REQUEST);
        }
        
        if (request.getXPosition() == null || request.getYPosition() == null) {
            log.error("Invalid move request - null positions: xPosition={}, yPosition={}", 
                    request.getXPosition(), request.getYPosition());
            throw new CustomException("Position coordinates cannot be null", StatusCode.INVALID_REQUEST);
        }
        
        log.debug("Processing move for room {} by user {} at position ({}, {})", 
                roomId, userId, request.getXPosition(), request.getYPosition());

        // First try regular find to get the room
        GameRoom room = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(StatusCode.ROOM_NOT_FOUND));
        
        // Game must be in progress to make moves
        if (room.getGameState() != GameState.IN_PROGRESS) {
            log.error("Invalid game state for moves. Room {} is in state: {}", roomId, room.getGameState());
            throw new CustomException(StatusCode.GAME_NOT_ACTIVE);
        }
        
        // Get current active match with pessimistic locking to prevent race conditions
        GameMatch currentMatch = getCurrentActiveMatch(room);
        
        // If players are not loaded (lazy loading), reload with players for turn validation
        if (room.getRoomPlayers().isEmpty()) {
            room = gameRoomRepository.findByIdWithPlayers(roomId)
                    .orElseThrow(() -> new CustomException(StatusCode.ROOM_NOT_FOUND));
        }
        
        // Validate player is in the room and it's their turn FIRST (before board operations)
        validatePlayerTurn(room, currentMatch, userId);
        
        // Get current board state and validate move atomically
        int[][] board = getCurrentBoard(roomId);
        if (!isValidMove(board, request.getXPosition(), request.getYPosition())) {
            throw new CustomException(StatusCode.INVALID_GAME_MOVE);
        }
        
        // Save the move to database FIRST to ensure consistency
        Move move = createAndSaveMove(currentMatch, userId, request);
        
        // Update in-memory board state
        int playerValue = getPlayerValueByUser(currentMatch, userId);
        board[request.getXPosition()][request.getYPosition()] = playerValue;
        
        // Check for win condition
        boolean hasWon = checkWin(board, request.getXPosition(), request.getYPosition(), playerValue);
        boolean isDraw = !hasWon && checkDraw(board);
        
        // Update game state if game ended
        if (hasWon || isDraw) {
            endGame(currentMatch, room, hasWon ? userId : null, isDraw);
        }
        
        // Build response
        GameMoveResponse response = buildMoveResponse(room, currentMatch, move, board, hasWon, isDraw, userId);
        
        // Broadcast move to all players in room
        broadcastMove(roomId, response);
        
        log.debug("Move processed successfully for room {}: {}", roomId, response);
        return response;
    }

    @Override
    public int[][] getCurrentBoard(Long roomId) {
        GameRoom room = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(StatusCode.ROOM_NOT_FOUND));
        
        GameMatch currentMatch = getCurrentActiveMatch(room);
        
        // Initialize empty board
        int[][] board = initializeBoard();
        
        // Get all moves for current match and apply them to board
        List<Move> moves = moveRepository.findByMatchOrderByMoveNumber(currentMatch);
        for (Move move : moves) {
            int playerValue = getPlayerValueByUser(currentMatch, move.getPlayer().getId());
            board[move.getXPosition()][move.getYPosition()] = playerValue;
        }
        
        return board;
    }

    @Override
    public boolean isValidMove(int[][] board, int xPosition, int yPosition) {
        // Check bounds
        if (xPosition < 0 || xPosition >= CaroGameConstants.BOARD_SIZE || 
            yPosition < 0 || yPosition >= CaroGameConstants.BOARD_SIZE) {
            return false;
        }
        
        // Check if cell is empty
        return board[xPosition][yPosition] == CaroGameConstants.EMPTY_CELL;
    }

    @Override
    public boolean checkWin(int[][] board, int xPosition, int yPosition, int playerValue) {
        // Check all four directions from the placed piece
        for (int[] direction : CaroGameConstants.WIN_DIRECTIONS) {
            int count = 1; // Count the current piece
            
            // Count in positive direction
            count += countInDirection(board, xPosition, yPosition, direction[0], direction[1], playerValue);
            
            // Count in negative direction
            count += countInDirection(board, xPosition, yPosition, -direction[0], -direction[1], playerValue);
            
            if (count >= CaroGameConstants.WIN_COUNT) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean checkDraw(int[][] board) {
        // Check if all cells are filled
        for (int i = 0; i < CaroGameConstants.BOARD_SIZE; i++) {
            for (int j = 0; j < CaroGameConstants.BOARD_SIZE; j++) {
                if (board[i][j] == CaroGameConstants.EMPTY_CELL) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int[][] initializeBoard() {
        return new int[CaroGameConstants.BOARD_SIZE][CaroGameConstants.BOARD_SIZE];
    }

    @Override
    public int getPlayerValue(Long roomId, Long userId) {
        GameRoom room = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(StatusCode.ROOM_NOT_FOUND));
        
        GameMatch currentMatch = getCurrentActiveMatch(room);
        return getPlayerValueByUser(currentMatch, userId);
    }

    @Override
    public String getPlayerSymbol(Long roomId, Long userId) {
        int playerValue = getPlayerValue(roomId, userId);
        return playerValue == CaroGameConstants.PLAYER_X_VALUE ? 
                CaroGameConstants.PLAYER_X_SYMBOL : 
                CaroGameConstants.PLAYER_O_SYMBOL;
    }
    
    /**
     * Resets the game board and starts a new match in the same room
     */
    public void resetGameBoard(Long roomId) {
        GameRoom room = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(StatusCode.ROOM_NOT_FOUND));
        
        // Reset room state
        room.setGameState(GameState.WAITING_FOR_READY);
        room.setGameStartedAt(null);
        room.setGameEndedAt(null);
        
        // Reset all players to not ready
        List<RoomPlayer> players = roomPlayerRepository.findByRoomId(roomId);
        for (RoomPlayer player : players) {
            player.setReadyState(PlayerReadyState.NOT_READY);
            roomPlayerRepository.save(player);
        }
        
        gameRoomRepository.save(room);
        
        log.info("Game board reset for room {}", roomId);
    }

    /**
     * Validates that the game room exists and is in the correct state for gameplay.
     */

    /**
     * Gets the current active match for the room.
     * Creates a new match if room is IN_PROGRESS but no active match exists.
     * Ensures proper player assignment based on join order.
     */
    private GameMatch getCurrentActiveMatch(GameRoom room) {
        java.util.Optional<GameMatch> existingMatch = gameMatchRepository.findByRoomAndResult(room, GameResult.ONGOING);
        
        if (existingMatch.isPresent()) {
            GameMatch match = existingMatch.get();
            // Verify match has proper player assignment
            if (match.getPlayerX() == null || match.getPlayerO() == null) {
                log.warn("Match {} has missing player assignments, fixing...", match.getId());
                assignPlayersToMatch(match, room);
                gameMatchRepository.save(match);
            }
            return match;
        }
        
        // If room is in IN_PROGRESS state but no active match exists, create one
        if (room.getGameState() == GameState.IN_PROGRESS) {
            return createNewMatchForRoom(room);
        }
        
        throw new CustomException(StatusCode.NO_ACTIVE_MATCH);
    }
    
    /**
     * Creates a new match for the room with proper player assignment.
     */
    private GameMatch createNewMatchForRoom(GameRoom room) {
        GameMatch newMatch = new GameMatch();
        newMatch.setRoom(room);
        newMatch.setResult(GameResult.ONGOING);
        newMatch.setStartTime(LocalDateTime.now());
        
        // Assign players based on room join order
        assignPlayersToMatch(newMatch, room);
        
        GameMatch savedMatch = gameMatchRepository.save(newMatch);
        log.info("Created new GameMatch with ID: {} for room {}", savedMatch.getId(), room.getId());
        return savedMatch;
    }
    
    /**
     * Assigns players to match based on room join order or host status.
     */
    private void assignPlayersToMatch(GameMatch match, GameRoom room) {
        java.util.List<RoomPlayer> roomPlayers = new java.util.ArrayList<>(room.getRoomPlayers());
        
        if (roomPlayers.size() < 2) {
            throw new CustomException("Room must have exactly 2 players to start game", StatusCode.INVALID_GAME_STATE);
        }
        
        // Sort by host status first, then by join order (id creation time)
        roomPlayers.sort((p1, p2) -> {
            if (p1.getIsHost() && !p2.getIsHost()) return -1;
            if (!p1.getIsHost() && p2.getIsHost()) return 1;
            return p1.getId().getRoomId().compareTo(p2.getId().getRoomId());
        });
        
        // Host or first player is X (goes first), second player is O
        match.setPlayerX(roomPlayers.get(0).getUser());
        match.setPlayerO(roomPlayers.get(1).getUser());
        
        log.debug("Assigned PlayerX: {} (ID={}), PlayerO: {} (ID={})", 
                roomPlayers.get(0).getUser().getUsername(), roomPlayers.get(0).getUser().getId(),
                roomPlayers.get(1).getUser().getUsername(), roomPlayers.get(1).getUser().getId());
    }

    /**
     * Validates that the player is in the room and it's their turn.
     * Uses database count to ensure thread safety.
     */
    private void validatePlayerTurn(GameRoom room, GameMatch currentMatch, Long userId) {
        // Check if player is in the room
        boolean isPlayerInRoom = room.getRoomPlayers().stream()
                .anyMatch(rp -> rp.getUser().getId().equals(userId));
        
        if (!isPlayerInRoom) {
            throw new CustomException(StatusCode.PLAYER_NOT_IN_ROOM);
        }
        
        // Ensure match has both players assigned
        if (currentMatch.getPlayerX() == null || currentMatch.getPlayerO() == null) {
            log.error("Match {} missing player assignments: PlayerX={}, PlayerO={}", 
                    currentMatch.getId(), currentMatch.getPlayerX(), currentMatch.getPlayerO());
            throw new CustomException(StatusCode.INVALID_GAME_STATE);
        }
        
        // Use database count for thread-safe turn validation
        int moveCount = moveRepository.countByMatch(currentMatch);
        boolean isPlayerXTurn = moveCount % 2 == 0; // X goes first, even moves
        
        int playerValue = getPlayerValueByUser(currentMatch, userId);
        
        // Debug logging
        log.debug("Turn validation: userId={}, moveCount={}, isPlayerXTurn={}, playerValue={}, PlayerX_ID={}, PlayerO_ID={}", 
                userId, moveCount, isPlayerXTurn, playerValue, 
                currentMatch.getPlayerX().getId(), currentMatch.getPlayerO().getId());
        
        // Validate it's the correct player's turn
        if ((isPlayerXTurn && playerValue != CaroGameConstants.PLAYER_X_VALUE) ||
            (!isPlayerXTurn && playerValue != CaroGameConstants.PLAYER_O_VALUE)) {
            log.error("Turn validation failed: Expected {} turn but user {} has value {}", 
                    isPlayerXTurn ? "X" : "O", userId, playerValue);
            throw new CustomException(StatusCode.NOT_PLAYER_TURN);
        }
        
        log.debug("Turn validation passed for user {}", userId);
    }

    /**
     * Creates and saves a move to the database.
     */
    private Move createAndSaveMove(GameMatch match, Long userId, GameMoveRequest request) {
        Move move = new Move();
        move.setMatch(match);
        move.setPlayer(userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(StatusCode.USER_NOT_FOUND)));
        move.setXPosition(request.getXPosition());
        move.setYPosition(request.getYPosition());
        
        // Calculate move number
        int moveCount = moveRepository.countByMatch(match);
        move.setMoveNumber(moveCount + 1);
        
        return moveRepository.save(move);
    }

    /**
     * Ends the game and updates the match and room state.
     * Ensures consistent state transitions and proper history saving.
     */
    private void endGame(GameMatch match, GameRoom room, Long winnerId, boolean isDraw) {
        match.setEndTime(LocalDateTime.now());
        room.setGameEndedAt(LocalDateTime.now());
        
        if (isDraw) {
            match.setResult(GameResult.DRAW);
            room.setGameState(GameState.FINISHED);
            room.setStatus(RoomStatus.FINISHED); // Fix: Also set room status to FINISHED
            saveGameHistoryForDraw(room);
            log.info("Game ended in draw for room {}", room.getId());
        } else {
            // Determine winner and loser
            int winnerValue = getPlayerValueByUser(match, winnerId);
            match.setResult(winnerValue == CaroGameConstants.PLAYER_X_VALUE ? 
                    GameResult.X_WIN : GameResult.O_WIN);
            room.setGameState(GameState.FINISHED);
            room.setStatus(RoomStatus.FINISHED); // Fix: Also set room status to FINISHED
            
            // Determine loser
            Long loserId = winnerId.equals(match.getPlayerX().getId()) ? 
                    match.getPlayerO().getId() : match.getPlayerX().getId();
            
            // Save game history for normal win
            saveGameHistory(room, winnerId, loserId, GameEndReason.WIN);
            log.info("Game ended with winner {} in room {}", winnerId, room.getId());
        }
        
        // Save entities in correct order
        gameMatchRepository.save(match);
        gameRoomRepository.save(room);
        
        log.info("Game ended in room {}: isDraw={}, winnerId={}", room.getId(), isDraw, winnerId);
    }
    
    /**
     * Saves game history when a player wins
     */
    private void saveGameHistory(GameRoom room, Long winnerId, Long loserId, GameEndReason endReason) {
        try {
            log.info("Saving game history for room {} - winner: {}, loser: {}, reason: {}", 
                    room.getId(), winnerId, loserId, endReason);
            
            GameHistory gameHistory = new GameHistory();
            gameHistory.setRoomId(room.getId());
            gameHistory.setWinnerId(winnerId);
            gameHistory.setLoserId(loserId);
            gameHistory.setEndReason(endReason);
            gameHistory.setGameStartedAt(room.getGameStartedAt());
            gameHistory.setGameEndedAt(room.getGameEndedAt());
            
            gameHistoryRepository.save(gameHistory);
            log.info("Game history saved successfully for room {}", room.getId());
        } catch (Exception e) {
            log.error("Error saving game history for room {}: {}", room.getId(), e.getMessage());
        }
    }
    
    /**
     * Saves game history for both players in case of draw
     */
    private void saveGameHistoryForDraw(GameRoom room) {
        try {
            List<RoomPlayer> players = roomPlayerRepository.findByRoomId(room.getId());
            if (players.size() >= 2) {
                // Create ONE history entry for draw game (not one per player)
                GameHistory gameHistory = new GameHistory();
                gameHistory.setRoomId(room.getId());
                gameHistory.setWinnerId(null); // No winner in draw
                gameHistory.setLoserId(null);  // No loser in draw
                gameHistory.setEndReason(GameEndReason.WIN); // Use WIN for completed games (even draws)
                gameHistory.setGameStartedAt(room.getGameStartedAt());
                gameHistory.setGameEndedAt(room.getGameEndedAt());
                
                gameHistoryRepository.save(gameHistory);
                log.info("Draw game history saved for room {}", room.getId());
            }
        } catch (Exception e) {
            log.error("Error saving draw game history for room {}: {}", room.getId(), e.getMessage());
        }
    }

    /**
     * Builds the response object for a move.
     */
    private GameMoveResponse buildMoveResponse(GameRoom room, GameMatch match, Move move, 
                                             int[][] board, boolean hasWon, boolean isDraw, Long currentPlayerId) {
        
        Long nextTurnPlayerId = null;
        if (!hasWon && !isDraw) {
            // Determine next player
            nextTurnPlayerId = currentPlayerId.equals(match.getPlayerX().getId()) ? 
                    match.getPlayerO().getId() : match.getPlayerX().getId();
        }
        
        // Get player symbol directly from the match instead of calling getPlayerSymbol()
        int playerValue = getPlayerValueByUser(match, currentPlayerId);
        String playerSymbol = playerValue == CaroGameConstants.PLAYER_X_VALUE ? 
                CaroGameConstants.PLAYER_X_SYMBOL : 
                CaroGameConstants.PLAYER_O_SYMBOL;
        
        return GameMoveResponse.builder()
                .roomId(room.getId())
                .matchId(match.getId())
                .xPosition(move.getXPosition())
                .yPosition(move.getYPosition())
                .playerId(currentPlayerId)
                .playerSymbol(playerSymbol)
                .moveNumber(move.getMoveNumber())
                .nextTurnPlayerId(nextTurnPlayerId)
                .gameState(room.getGameState())
                .gameResult(match.getResult())
                .winnerId(hasWon ? currentPlayerId : null)
                .board(board)
                .isValidMove(true)
                .message(hasWon ? CaroGameConstants.MSG_GAME_WON : 
                        (isDraw ? CaroGameConstants.MSG_GAME_DRAW : "Move successful"))
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    /**
     * Broadcasts the move to all players in the room via WebSocket.
     */
    private void broadcastMove(Long roomId, GameMoveResponse response) {
        String topic = String.format(CaroGameConstants.TOPIC_GAME_MOVE, roomId);
        messagingTemplate.convertAndSend(topic, response);
        
        // If game ended, also send to game end topic
        if (response.getGameState() == GameState.FINISHED) {
            String endTopic = String.format(CaroGameConstants.TOPIC_GAME_END, roomId);
            messagingTemplate.convertAndSend(endTopic, response);
        }
    }

    /**
     * Counts consecutive pieces in a given direction.
     */
    private int countInDirection(int[][] board, int startX, int startY, int dirX, int dirY, int playerValue) {
        int count = 0;
        int x = startX + dirX;
        int y = startY + dirY;
        
        while (x >= 0 && x < CaroGameConstants.BOARD_SIZE && 
               y >= 0 && y < CaroGameConstants.BOARD_SIZE && 
               board[x][y] == playerValue) {
            count++;
            x += dirX;
            y += dirY;
        }
        
        return count;
    }

    /**
     * Gets the player value for a user in a specific match.
     */
    private int getPlayerValueByUser(GameMatch match, Long userId) {
        if (match.getPlayerX() != null && match.getPlayerX().getId().equals(userId)) {
            return CaroGameConstants.PLAYER_X_VALUE;
        } else if (match.getPlayerO() != null && match.getPlayerO().getId().equals(userId)) {
            return CaroGameConstants.PLAYER_O_VALUE;
        }
        return CaroGameConstants.EMPTY_CELL;
    }
}
