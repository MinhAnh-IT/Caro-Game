package com.vn.caro_game.services;

import com.vn.caro_game.dtos.request.CreateRoomRequest;
import com.vn.caro_game.dtos.response.GameRoomResponse;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuickPlayIssueTest {

    @Autowired
    private GameRoomService gameRoomService;

    @Autowired
    private GameRoomRepository gameRoomRepository;

    @Autowired
    private RoomPlayerRepository roomPlayerRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("quickplayer");
        testUser.setEmail("quickplayer@test.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testQuickPlayAfterFinishedGameScenario() {
        System.out.println("=== Testing Quick Play Issue Scenario ===");
        
        // Step 1: User creates/joins a room and finishes the game
        CreateRoomRequest createRequest = new CreateRoomRequest();
        createRequest.setName("Test Game Room");
        createRequest.setIsPrivate(false);
        
        GameRoomResponse gameRoom = gameRoomService.createRoom(createRequest, testUser.getId());
        Long roomId = gameRoom.getId();
        
        System.out.println("1. Created room: " + roomId);
        
        // Create second user to play
        User secondUser = new User();
        secondUser.setUsername("player2");
        secondUser.setEmail("player2@test.com");
        secondUser.setPassword("password");
        secondUser = userRepository.save(secondUser);
        
        // Second user joins
        gameRoomService.joinRoom(roomId, secondUser.getId());
        System.out.println("2. Second user joined room: " + roomId);
        
        // Start and finish the game
        GameRoom room = gameRoomRepository.findById(roomId).orElse(null);
        room.setStatus(RoomStatus.PLAYING);
        room.setGameStartedAt(LocalDateTime.now());
        gameRoomRepository.save(room);
        
        // Complete the game (testUser wins)
        gameRoomService.completeGame(roomId, testUser.getId(), secondUser.getId());
        System.out.println("3. Game completed in room: " + roomId);
        
        // Verify room is FINISHED
        room = gameRoomRepository.findById(roomId).orElse(null);
        assertEquals(RoomStatus.FINISHED, room.getStatus());
        System.out.println("4. Room status confirmed as FINISHED");
        
        // ===== SIMULATE USER MOVING TO ANOTHER ROOM =====
        System.out.println("5. Simulating user moving to another room...");
        
        // Create another room
        CreateRoomRequest anotherRequest = new CreateRoomRequest();
        anotherRequest.setName("Another Room");
        anotherRequest.setIsPrivate(false);
        
        GameRoomResponse anotherRoom = gameRoomService.createRoom(anotherRequest, testUser.getId());
        Long anotherRoomId = anotherRoom.getId();
        System.out.println("   User created another room: " + anotherRoomId);
        
        // Check user's current room status after moving to another room
        List<RoomPlayer> userRoomPlayers = roomPlayerRepository.findByUserId(testUser.getId());
        System.out.println("6. After creating another room, user is in " + userRoomPlayers.size() + " rooms total");
        for (RoomPlayer rp : userRoomPlayers) {
            GameRoom r = rp.getRoom();
            System.out.println("   Room " + r.getId() + " - Status: " + r.getStatus() + " - GameState: " + r.getGameState());
        }
        
        // Check if user is considered "in active room"
        boolean isInActiveRoom = gameRoomRepository.existsActiveRoomByUserId(testUser.getId());
        System.out.println("7. User is in active room (WAITING/PLAYING): " + isInActiveRoom);
        
        // Now user leaves the second room to test the scenario you described
        System.out.println("8. User leaves the second room...");
        gameRoomService.leaveRoom(anotherRoomId, testUser.getId());
        
        // Check status after leaving
        userRoomPlayers = roomPlayerRepository.findByUserId(testUser.getId());
        System.out.println("9. After leaving second room, user is in " + userRoomPlayers.size() + " rooms total");
        for (RoomPlayer rp : userRoomPlayers) {
            GameRoom r = rp.getRoom();
            System.out.println("   Room " + r.getId() + " - Status: " + r.getStatus() + " - GameState: " + r.getGameState());
        }
        
        isInActiveRoom = gameRoomRepository.existsActiveRoomByUserId(testUser.getId());
        System.out.println("10. After leaving, user is in active room: " + isInActiveRoom);
        
        // Step 2: User tries quick play
        System.out.println("11. Attempting quick play after leaving...");
        GameRoomResponse quickPlayRoom = gameRoomService.findOrCreatePublicRoom(testUser.getId());
        
        System.out.println("12. Quick play returned room: " + quickPlayRoom.getId());
        System.out.println("    Room status: " + quickPlayRoom.getStatus());
        System.out.println("    Room game state: " + quickPlayRoom.getGameState());
        
        // Check if this is the same finished room (THIS IS THE BUG)
        if (quickPlayRoom.getId().equals(roomId)) {
            System.out.println("❌ BUG CONFIRMED: Quick play returned the finished room!");
            System.out.println("   Finished room ID: " + roomId);
            System.out.println("   Quick play room ID: " + quickPlayRoom.getId());
            
            // This should NOT happen - user should get a new room or join available room
            fail("Quick play returned finished room - this is the bug!");
        } else {
            System.out.println("✅ Quick play correctly returned different room: " + quickPlayRoom.getId());
        }
        
        // Check final state
        List<RoomPlayer> finalUserRoomPlayers = roomPlayerRepository.findByUserId(testUser.getId());
        System.out.println("13. After quick play, user is in " + finalUserRoomPlayers.size() + " rooms total");
        for (RoomPlayer rp : finalUserRoomPlayers) {
            GameRoom r = rp.getRoom();
            System.out.println("    Room " + r.getId() + " - Status: " + r.getStatus() + " - GameState: " + r.getGameState());
        }
        
        System.out.println("=== Test Complete ===");
    }
}
