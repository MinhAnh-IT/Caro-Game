package com.vn.caro_game.services;

import com.vn.caro_game.entities.*;
import com.vn.caro_game.enums.*;
import com.vn.caro_game.repositories.*;
import com.vn.caro_game.services.interfaces.GameRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RematchIntegrationTest {

    @Autowired
    private GameRoomService gameRoomService;

    @Autowired
    private GameRoomRepository gameRoomRepository;

    @Autowired
    private RoomPlayerRepository roomPlayerRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private GameRoom finishedRoom;

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

        // Create a finished game room
        finishedRoom = new GameRoom();
        finishedRoom.setName("Test Room");
        finishedRoom.setIsPrivate(false);
        finishedRoom.setStatus(RoomStatus.FINISHED);
        finishedRoom.setGameState(GameState.FINISHED);
        finishedRoom.setCreatedBy(user1);
        finishedRoom = gameRoomRepository.save(finishedRoom);

        // Add players to finished room
        RoomPlayer player1 = new RoomPlayer();
        player1.setId(new RoomPlayer.RoomPlayerId(finishedRoom.getId(), user1.getId()));
        player1.setRoom(finishedRoom);
        player1.setUser(user1);
        player1.setIsHost(true);
        player1.setGameResult(GameResult.WIN);
        roomPlayerRepository.save(player1);

        RoomPlayer player2 = new RoomPlayer();
        player2.setId(new RoomPlayer.RoomPlayerId(finishedRoom.getId(), user2.getId()));
        player2.setRoom(finishedRoom);
        player2.setUser(user2);
        player2.setIsHost(false);
        player2.setGameResult(GameResult.LOSE);
        roomPlayerRepository.save(player2);
    }

    @Test
    void testRematchFlow_ShouldMovePlayersToNewRoom() {
        // Verify initial state
        List<RoomPlayer> initialPlayers = roomPlayerRepository.findByRoomId(finishedRoom.getId());
        assertEquals(2, initialPlayers.size(), "Should have 2 players in original room");

        // Re-fetch room to ensure relationships are loaded
        finishedRoom = gameRoomRepository.findById(finishedRoom.getId()).orElse(null);
        assertNotNull(finishedRoom);

        // Verify room state for rematch
        assertEquals(2, roomPlayerRepository.findByRoomId(finishedRoom.getId()).size(), 
            "Room should have 2 players for rematch");

        // Verify users are in finished room
        assertTrue(gameRoomRepository.existsActiveRoomByUserId(user1.getId()) == false, 
            "User1 should not be in active room (finished room doesn't count)");
        assertTrue(gameRoomRepository.existsActiveRoomByUserId(user2.getId()) == false, 
            "User2 should not be in active room (finished room doesn't count)");

        // Step 1: Request rematch
        gameRoomService.requestRematch(finishedRoom.getId(), user1.getId());
        
        GameRoom updatedRoom = gameRoomRepository.findById(finishedRoom.getId()).orElse(null);
        assertNotNull(updatedRoom);
        assertEquals(RematchState.REQUESTED, updatedRoom.getRematchState());
        assertEquals(user1.getId(), updatedRoom.getRematchRequesterId());

        // Step 2: Accept rematch
        gameRoomService.acceptRematch(finishedRoom.getId(), user2.getId());

        // Verify rematch was created
        updatedRoom = gameRoomRepository.findById(finishedRoom.getId()).orElse(null);
        assertNotNull(updatedRoom);
        assertEquals(RematchState.CREATED, updatedRoom.getRematchState());
        assertNotNull(updatedRoom.getNewRoomId());

        Long newRoomId = updatedRoom.getNewRoomId();
        GameRoom newRoom = gameRoomRepository.findById(newRoomId).orElse(null);
        assertNotNull(newRoom, "New rematch room should be created");

        // Verify new room properties
        assertEquals(RoomStatus.WAITING, newRoom.getStatus());
        assertEquals(GameState.WAITING_FOR_READY, newRoom.getGameState());
        assertTrue(newRoom.getName().contains("Rematch"));

        // CRITICAL: Verify players are moved to new room
        List<RoomPlayer> newRoomPlayers = roomPlayerRepository.findByRoomId(newRoomId);
        assertEquals(2, newRoomPlayers.size(), "New room should have 2 players");

        // Verify players are no longer in old room
        List<RoomPlayer> oldRoomPlayers = roomPlayerRepository.findByRoomId(finishedRoom.getId());
        assertEquals(0, oldRoomPlayers.size(), "Old room should have no players");

        // Verify player properties in new room
        for (RoomPlayer player : newRoomPlayers) {
            assertEquals(PlayerReadyState.NOT_READY, player.getReadyState());
            assertEquals(GameResult.NONE, player.getGameResult());
            assertFalse(player.getAcceptedRematch());
            assertFalse(player.getHasLeft());
        }

        // Verify users are now in active room
        assertTrue(gameRoomRepository.existsActiveRoomByUserId(user1.getId()), 
            "User1 should now be in active room (new rematch room)");
        assertTrue(gameRoomRepository.existsActiveRoomByUserId(user2.getId()), 
            "User2 should now be in active room (new rematch room)");
    }

    @Test
    void testRematchFlow_UserNotInMultipleRooms() {
        // Re-fetch room to ensure relationships are loaded
        finishedRoom = gameRoomRepository.findById(finishedRoom.getId()).orElse(null);
        assertNotNull(finishedRoom);

        // Request and accept rematch
        gameRoomService.requestRematch(finishedRoom.getId(), user1.getId());
        gameRoomService.acceptRematch(finishedRoom.getId(), user2.getId());

        // Get new room
        GameRoom updatedRoom = gameRoomRepository.findById(finishedRoom.getId()).orElse(null);
        Long newRoomId = updatedRoom.getNewRoomId();

        // Verify each user is only in one active room
        List<GameRoom> user1ActiveRooms = gameRoomRepository.findActiveRoomsByUserId(
            user1.getId(), org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(1, user1ActiveRooms.size(), "User1 should be in exactly 1 active room");
        assertEquals(newRoomId, user1ActiveRooms.get(0).getId());

        List<GameRoom> user2ActiveRooms = gameRoomRepository.findActiveRoomsByUserId(
            user2.getId(), org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(1, user2ActiveRooms.size(), "User2 should be in exactly 1 active room");
        assertEquals(newRoomId, user2ActiveRooms.get(0).getId());
    }
}
