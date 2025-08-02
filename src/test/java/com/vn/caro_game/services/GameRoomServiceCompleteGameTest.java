package com.vn.caro_game.services;

import com.vn.caro_game.entities.GameRoom;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.GameState;
import com.vn.caro_game.enums.RoomStatus;
import com.vn.caro_game.repositories.GameRoomRepository;
import com.vn.caro_game.repositories.UserRepository;
import com.vn.caro_game.services.interfaces.GameRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Simple test to verify GameRoomService.completeGame sets status correctly
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GameRoomServiceCompleteGameTest {

    @Autowired
    private GameRoomService gameRoomService;

    @Autowired
    private GameRoomRepository gameRoomRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private GameRoom gameRoom;

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

        // Create a PLAYING game room
        gameRoom = new GameRoom();
        gameRoom.setName("Test Room");
        gameRoom.setIsPrivate(false);
        gameRoom.setStatus(RoomStatus.PLAYING);
        gameRoom.setGameState(GameState.IN_PROGRESS);
        gameRoom.setGameStartedAt(LocalDateTime.now());
        gameRoom.setCreatedBy(user1);
        gameRoom = gameRoomRepository.save(gameRoom);
    }

    @Test
    void testCompleteGameSetsStatusToFinished() {
        System.out.println("=== Test: GameRoomService.completeGame sets status to FINISHED ===");
        
        // Verify initial state
        assertEquals(RoomStatus.PLAYING, gameRoom.getStatus());
        assertEquals(GameState.IN_PROGRESS, gameRoom.getGameState());
        System.out.println("Initial room status: " + gameRoom.getStatus());
        System.out.println("Initial game state: " + gameRoom.getGameState());

        // Complete the game using GameRoomService.completeGame
        gameRoomService.completeGame(gameRoom.getId(), user1.getId(), user2.getId());

        // Verify final state
        gameRoom = gameRoomRepository.findById(gameRoom.getId()).orElse(null);
        System.out.println("Final room status: " + gameRoom.getStatus());
        System.out.println("Final game state: " + gameRoom.getGameState());

        // Verify both status and gameState are set to FINISHED
        assertEquals(RoomStatus.FINISHED, gameRoom.getStatus(), "Room status should be FINISHED after completeGame");
        assertEquals(GameState.FINISHED, gameRoom.getGameState(), "Game state should be FINISHED after completeGame");

        System.out.println("✅ Test passed: GameRoomService.completeGame properly sets status to FINISHED");
    }
}
