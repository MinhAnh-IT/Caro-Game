package com.vn.caro_game.services;

import com.vn.caro_game.dtos.request.GameMoveRequest;
import com.vn.caro_game.dtos.response.GameMoveResponse;
import com.vn.caro_game.entities.GameMatch;
import com.vn.caro_game.entities.GameRoom;
import com.vn.caro_game.entities.Move;
import com.vn.caro_game.entities.RoomPlayer;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.GameResult;
import com.vn.caro_game.enums.GameState;
import com.vn.caro_game.enums.RoomStatus;
import com.vn.caro_game.repositories.GameMatchRepository;
import com.vn.caro_game.repositories.GameRoomRepository;
import com.vn.caro_game.repositories.MoveRepository;
import com.vn.caro_game.repositories.RoomPlayerRepository;
import com.vn.caro_game.repositories.UserRepository;
import com.vn.caro_game.repositories.GameHistoryRepository;
import com.vn.caro_game.services.interfaces.CaroGameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test to verify that game completion properly sets room status to FINISHED
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GameCompletionStatusTest {

    @Autowired
    private CaroGameService caroGameService;

    @Autowired
    private GameRoomRepository gameRoomRepository;

    @Autowired
    private GameMatchRepository gameMatchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomPlayerRepository roomPlayerRepository;

    @Autowired
    private MoveRepository moveRepository;

    @Autowired
    private GameHistoryRepository gameHistoryRepository;

    private User user1;
    private User user2;
    private GameRoom gameRoom;
    private GameMatch gameMatch;

    @BeforeEach
    void setUp() {
        // Create test users
        user1 = new User();
        user1.setUsername("player1");
        user1.setEmail("player1@test.com");
        user1.setPassword("password");
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setUsername("player2");
        user2.setEmail("player2@test.com");
        user2.setPassword("password");
        user2 = userRepository.save(user2);

        // Create game room
        gameRoom = new GameRoom();
        gameRoom.setName("Test Room");
        gameRoom.setIsPrivate(false);
        gameRoom.setStatus(RoomStatus.PLAYING);
        gameRoom.setGameState(GameState.IN_PROGRESS);
        gameRoom.setGameStartedAt(LocalDateTime.now());
        gameRoom.setCreatedBy(user1);
        gameRoom = gameRoomRepository.save(gameRoom);

        // Add players to room
        RoomPlayer player1 = new RoomPlayer();
        player1.setId(new RoomPlayer.RoomPlayerId(gameRoom.getId(), user1.getId()));
        player1.setRoom(gameRoom);
        player1.setUser(user1);
        player1.setIsHost(true);
        roomPlayerRepository.save(player1);

        RoomPlayer player2 = new RoomPlayer();
        player2.setId(new RoomPlayer.RoomPlayerId(gameRoom.getId(), user2.getId()));
        player2.setRoom(gameRoom);
        player2.setUser(user2);
        player2.setIsHost(false);
        roomPlayerRepository.save(player2);

        // Create game match
        gameMatch = new GameMatch();
        gameMatch.setRoom(gameRoom);
        gameMatch.setPlayerX(user1);
        gameMatch.setPlayerO(user2);
        gameMatch.setResult(GameResult.ONGOING);
        gameMatch.setStartTime(LocalDateTime.now());
        gameMatch = gameMatchRepository.save(gameMatch);
    }

    @Test
    void testGameCompletionSetsRoomStatusToFinished() {
        System.out.println("=== Test: Game Completion Sets Room Status to FINISHED ===");
        
        // Verify initial state
        gameRoom = gameRoomRepository.findById(gameRoom.getId()).orElse(null);
        assertEquals(RoomStatus.PLAYING, gameRoom.getStatus());
        assertEquals(GameState.IN_PROGRESS, gameRoom.getGameState());
        System.out.println("Initial room status: " + gameRoom.getStatus());
        System.out.println("Initial game state: " + gameRoom.getGameState());

        // Reload room with players for validation
        gameRoom = gameRoomRepository.findById(gameRoom.getId()).orElse(null);
        
        // Create a winning move pattern for user1 (X)
        // Place 5 X's in a row horizontally to win
        makeMove(7, 7, user1); // X at (7,7)
        makeMove(8, 8, user2); // O at (8,8)
        makeMove(7, 8, user1); // X at (7,8)
        makeMove(8, 9, user2); // O at (8,9)
        makeMove(7, 9, user1); // X at (7,9)
        makeMove(8, 10, user2); // O at (8,10)
        makeMove(7, 10, user1); // X at (7,10)
        makeMove(8, 11, user2); // O at (8,11)
        
        // This move should win the game for user1 (5 in a row horizontally)
        GameMoveResponse winningMove = makeMove(7, 11, user1); // X at (7,11) - winning move

        System.out.println("Winning move response: " + winningMove.getMessage());
        System.out.println("Game state after winning move: " + winningMove.getGameState());
        System.out.println("Winner ID: " + winningMove.getWinnerId());

        // Verify game is completed and room status is FINISHED
        gameRoom = gameRoomRepository.findById(gameRoom.getId()).orElse(null);
        gameMatch = gameMatchRepository.findById(gameMatch.getId()).orElse(null);

        System.out.println("Final room status: " + gameRoom.getStatus());
        System.out.println("Final game state: " + gameRoom.getGameState());
        System.out.println("Final match result: " + gameMatch.getResult());

        // The fix should ensure both status and gameState are set to FINISHED
        assertEquals(RoomStatus.FINISHED, gameRoom.getStatus(), "Room status should be FINISHED after game completion");
        assertEquals(GameState.FINISHED, gameRoom.getGameState(), "Game state should be FINISHED after game completion");
        assertEquals(GameResult.X_WIN, gameMatch.getResult(), "Match result should be X_WIN");
        assertEquals(user1.getId(), winningMove.getWinnerId(), "Winner should be user1");

        System.out.println("✅ Test passed: Room status and game state properly set to FINISHED");
    }

    @Test
    void testDrawGameSetsRoomStatusToFinished() {
        System.out.println("=== Test: Draw Game Sets Room Status to FINISHED ===");
        
        // Fill the entire board to force a draw
        int[][] board = caroGameService.initializeBoard();
        
        // Alternate moves to fill board without creating 5 in a row
        boolean isPlayerXTurn = true;
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                // Skip certain positions to avoid creating 5 in a row
                if (wouldCreateWin(board, row, col, isPlayerXTurn ? 1 : 2)) {
                    continue;
                }
                
                User currentPlayer = isPlayerXTurn ? user1 : user2;
                makeMove(row, col, currentPlayer);
                board[row][col] = isPlayerXTurn ? 1 : 2;
                isPlayerXTurn = !isPlayerXTurn;
                
                // Check if board is full (draw condition)
                if (isBoardFull(board)) {
                    break;
                }
            }
            if (isBoardFull(board)) {
                break;
            }
        }

        // Verify final state
        gameRoom = gameRoomRepository.findById(gameRoom.getId()).orElse(null);
        gameMatch = gameMatchRepository.findById(gameMatch.getId()).orElse(null);

        System.out.println("Final room status: " + gameRoom.getStatus());
        System.out.println("Final game state: " + gameRoom.getGameState());
        System.out.println("Final match result: " + gameMatch.getResult());

        // For draw games, both status and gameState should be FINISHED
        assertEquals(RoomStatus.FINISHED, gameRoom.getStatus(), "Room status should be FINISHED after draw");
        assertEquals(GameState.FINISHED, gameRoom.getGameState(), "Game state should be FINISHED after draw");
        assertEquals(GameResult.DRAW, gameMatch.getResult(), "Match result should be DRAW");

        System.out.println("✅ Test passed: Draw game properly sets room status to FINISHED");
    }

    private GameMoveResponse makeMove(int x, int y, User player) {
        GameMoveRequest request = new GameMoveRequest();
        request.setXPosition(x);
        request.setYPosition(y);
        return caroGameService.makeMove(gameRoom.getId(), request, player.getId());
    }

    private boolean wouldCreateWin(int[][] board, int x, int y, int playerValue) {
        // Simple check to avoid creating 5 in a row
        // This is a simplified implementation for testing
        return false; // For now, don't avoid any moves to keep test simple
    }

    private boolean isBoardFull(int[][] board) {
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                if (board[row][col] == 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
