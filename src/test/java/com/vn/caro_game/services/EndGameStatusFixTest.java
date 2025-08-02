package com.vn.caro_game.services;

import com.vn.caro_game.entities.GameMatch;
import com.vn.caro_game.entities.GameRoom;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.GameResult;
import com.vn.caro_game.enums.GameState;
import com.vn.caro_game.enums.RoomStatus;
import com.vn.caro_game.repositories.GameMatchRepository;
import com.vn.caro_game.repositories.GameRoomRepository;
import com.vn.caro_game.repositories.UserRepository;
import com.vn.caro_game.services.impl.CaroGameServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Simple test to verify the endGame logic fix
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EndGameStatusFixTest {

    @Autowired
    private CaroGameServiceImpl caroGameService;

    @Autowired
    private GameRoomRepository gameRoomRepository;

    @Autowired
    private GameMatchRepository gameMatchRepository;

    @Autowired
    private UserRepository userRepository;

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
    void testEndGameSetsRoomStatusToFinished() {
        System.out.println("=== Test: endGame sets room status to FINISHED ===");
        
        // Verify initial state
        assertEquals(RoomStatus.PLAYING, gameRoom.getStatus());
        assertEquals(GameState.IN_PROGRESS, gameRoom.getGameState());
        System.out.println("Initial room status: " + gameRoom.getStatus());
        System.out.println("Initial game state: " + gameRoom.getGameState());

        // Call the private endGame method directly using reflection to test the fix
        // This simulates what happens when someone wins the game
        try {
            java.lang.reflect.Method endGameMethod = CaroGameServiceImpl.class.getDeclaredMethod(
                "endGame", GameMatch.class, GameRoom.class, Long.class, boolean.class);
            endGameMethod.setAccessible(true);
            
            // Call endGame with user1 as winner (not a draw)
            endGameMethod.invoke(caroGameService, gameMatch, gameRoom, user1.getId(), false);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to call endGame method", e);
        }

        // Verify game is completed and room status is FINISHED
        gameRoom = gameRoomRepository.findById(gameRoom.getId()).orElse(null);
        gameMatch = gameMatchRepository.findById(gameMatch.getId()).orElse(null);

        System.out.println("Final room status: " + gameRoom.getStatus());
        System.out.println("Final game state: " + gameRoom.getGameState());
        System.out.println("Final match result: " + gameMatch.getResult());

        // The fix should ensure both status and gameState are set to FINISHED
        assertEquals(RoomStatus.FINISHED, gameRoom.getStatus(), "Room status should be FINISHED after endGame");
        assertEquals(GameState.FINISHED, gameRoom.getGameState(), "Game state should be FINISHED after endGame");
        assertEquals(GameResult.X_WIN, gameMatch.getResult(), "Match result should be X_WIN");

        System.out.println("✅ Test passed: endGame properly sets room status to FINISHED");
    }

    @Test
    void testEndGameDrawSetsRoomStatusToFinished() {
        System.out.println("=== Test: endGame for draw sets room status to FINISHED ===");
        
        // Verify initial state
        assertEquals(RoomStatus.PLAYING, gameRoom.getStatus());
        assertEquals(GameState.IN_PROGRESS, gameRoom.getGameState());
        System.out.println("Initial room status: " + gameRoom.getStatus());
        System.out.println("Initial game state: " + gameRoom.getGameState());

        // Call the private endGame method directly using reflection for draw game
        try {
            java.lang.reflect.Method endGameMethod = CaroGameServiceImpl.class.getDeclaredMethod(
                "endGame", GameMatch.class, GameRoom.class, Long.class, boolean.class);
            endGameMethod.setAccessible(true);
            
            // Call endGame for draw (winnerId null, isDraw true)
            endGameMethod.invoke(caroGameService, gameMatch, gameRoom, null, true);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to call endGame method", e);
        }

        // Verify game is completed and room status is FINISHED
        gameRoom = gameRoomRepository.findById(gameRoom.getId()).orElse(null);
        gameMatch = gameMatchRepository.findById(gameMatch.getId()).orElse(null);

        System.out.println("Final room status: " + gameRoom.getStatus());
        System.out.println("Final game state: " + gameRoom.getGameState());
        System.out.println("Final match result: " + gameMatch.getResult());

        // The fix should ensure both status and gameState are set to FINISHED for draws too
        assertEquals(RoomStatus.FINISHED, gameRoom.getStatus(), "Room status should be FINISHED after draw");
        assertEquals(GameState.FINISHED, gameRoom.getGameState(), "Game state should be FINISHED after draw");
        assertEquals(GameResult.DRAW, gameMatch.getResult(), "Match result should be DRAW");

        System.out.println("✅ Test passed: endGame for draw properly sets room status to FINISHED");
    }
}
