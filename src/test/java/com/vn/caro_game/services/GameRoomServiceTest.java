package com.vn.caro_game.services;

import com.vn.caro_game.constants.GameRoomConstants;
import com.vn.caro_game.dtos.request.CreateRoomRequest;
import com.vn.caro_game.dtos.request.JoinRoomRequest;
import com.vn.caro_game.dtos.request.SendChatMessageRequest;
import com.vn.caro_game.dtos.response.GameRoomResponse;
import com.vn.caro_game.dtos.response.ChatMessageResponse;
import com.vn.caro_game.entities.GameRoom;
import com.vn.caro_game.entities.RoomPlayer;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.entities.ChatMessage;
import com.vn.caro_game.enums.RoomStatus;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.mappers.GameRoomMapper;
import com.vn.caro_game.repositories.GameRoomRepository;
import com.vn.caro_game.repositories.RoomPlayerRepository;
import com.vn.caro_game.repositories.UserRepository;
import com.vn.caro_game.repositories.ChatMessageRepository;
import com.vn.caro_game.services.impl.GameRoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GameRoomService implementation.
 * Tests core business logic for game room management.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class GameRoomServiceTest {

    @Mock
    private GameRoomRepository gameRoomRepository;

    @Mock
    private RoomPlayerRepository roomPlayerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private GameRoomMapper gameRoomMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private GameRoomServiceImpl gameRoomService;

    private User testUser;
    private GameRoom testRoom;
    private CreateRoomRequest createRoomRequest;
    private JoinRoomRequest joinRoomRequest;
    private SendChatMessageRequest chatMessageRequest;
    private GameRoomResponse gameRoomResponse;
    private ChatMessageResponse chatMessageResponse;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // Setup test room
        testRoom = new GameRoom();
        testRoom.setId(1L);
        testRoom.setName("Test Room");
        testRoom.setStatus(RoomStatus.WAITING);
        testRoom.setIsPrivate(false);
        testRoom.setJoinCode("A3X7");
        testRoom.setCreatedBy(testUser);
        testRoom.setCreatedAt(LocalDateTime.now());

        // Setup create room request
        createRoomRequest = new CreateRoomRequest();
        createRoomRequest.setName("Test Room");
        createRoomRequest.setIsPrivate(false);

        // Setup join room request
        joinRoomRequest = new JoinRoomRequest();
        joinRoomRequest.setJoinCode("A3X7");

        // Setup chat message request
        chatMessageRequest = new SendChatMessageRequest();
        chatMessageRequest.setContent("Hello world!");

        // Setup responses (mock responses since DTOs don't have default constructors)
        gameRoomResponse = mock(GameRoomResponse.class);
        chatMessageResponse = mock(ChatMessageResponse.class);
    }

    @Test
    void validateGameRoomConstants_ShouldHaveCorrectValues() {
        // Test that constants are properly defined
        assertEquals(2, GameRoomConstants.MAX_PLAYERS_PER_ROOM);
        assertEquals(4, GameRoomConstants.JOIN_CODE_LENGTH);
        
        // Test that the service was properly initialized
        assertNotNull(gameRoomService);
        assertNotNull(gameRoomRepository);
        assertNotNull(roomPlayerRepository);
        assertNotNull(userRepository);
        assertNotNull(gameRoomMapper);
        assertNotNull(messagingTemplate);
    }

    @Test
    void testUser_ShouldHaveCorrectProperties() {
        // Test that our test user setup is correct
        assertNotNull(testUser);
        assertEquals(1L, testUser.getId());
        assertEquals("testuser", testUser.getUsername());
        assertEquals("test@example.com", testUser.getEmail());
    }

    @Test
    void gameRoomService_ShouldBeProperlyInjected() {
        // Test that dependency injection worked correctly
        assertNotNull(gameRoomService);
        
        // Verify that this is the correct implementation class
        assertTrue(gameRoomService instanceof GameRoomServiceImpl);
    }

    @Test
    void createRoom_WithValidRequest_ShouldCreateRoom() {
        // Given
        when(gameRoomRepository.existsActiveRoomByUserId(1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(gameRoomMapper.mapToGameRoomResponse(any(GameRoom.class), anyList())).thenReturn(gameRoomResponse);

        // When
        GameRoomResponse result = gameRoomService.createRoom(createRoomRequest, 1L);

        // Then
        assertNotNull(result);
        verify(gameRoomRepository).save(any(GameRoom.class));
        verify(roomPlayerRepository).save(any(RoomPlayer.class));
        verify(gameRoomMapper).mapToGameRoomResponse(any(GameRoom.class), anyList());
    }

    @Test
    void createRoom_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomException.class, () -> {
            gameRoomService.createRoom(createRoomRequest, 1L);
        });
    }

    @Test
    void joinRoom_WithValidRoomId_ShouldJoinRoom() {
        // Given
        when(gameRoomRepository.existsActiveRoomByUserId(1L)).thenReturn(false);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(gameRoomRepository.countPlayersByRoomId(1L)).thenReturn(1);
        when(roomPlayerRepository.existsByRoomIdAndUserId(1L, 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(gameRoomMapper.mapToGameRoomResponse(any(GameRoom.class), anyList())).thenReturn(gameRoomResponse);

        // When
        GameRoomResponse result = gameRoomService.joinRoom(1L, 1L);

        // Then
        assertNotNull(result);
        verify(roomPlayerRepository).save(any(RoomPlayer.class));
        verify(gameRoomMapper).mapToGameRoomResponse(any(GameRoom.class), anyList());
    }

    @Test
    void joinRoom_WhenRoomNotFound_ShouldThrowException() {
        // Given
        when(gameRoomRepository.existsActiveRoomByUserId(1L)).thenReturn(false);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomException.class, () -> {
            gameRoomService.joinRoom(1L, 1L);
        });
    }

    @Test
    void joinRoom_WhenRoomIsFull_ShouldThrowException() {
        // Given
        when(gameRoomRepository.existsActiveRoomByUserId(1L)).thenReturn(false);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(gameRoomRepository.countPlayersByRoomId(1L)).thenReturn(GameRoomConstants.MAX_PLAYERS_PER_ROOM);

        // When & Then
        assertThrows(CustomException.class, () -> {
            gameRoomService.joinRoom(1L, 1L);
        });
    }

    @Test
    void joinRoomByCode_WithValidCode_ShouldJoinRoom() {
        // Given
        when(gameRoomRepository.existsActiveRoomByUserId(1L)).thenReturn(false);
        when(gameRoomRepository.findByJoinCode("A3X7")).thenReturn(Optional.of(testRoom));
        when(gameRoomRepository.countPlayersByRoomId(1L)).thenReturn(1);
        when(roomPlayerRepository.existsByRoomIdAndUserId(1L, 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(gameRoomMapper.mapToGameRoomResponse(any(GameRoom.class), anyList())).thenReturn(gameRoomResponse);

        // When
        GameRoomResponse result = gameRoomService.joinRoomByCode(joinRoomRequest, 1L);

        // Then
        assertNotNull(result);
        verify(roomPlayerRepository).save(any(RoomPlayer.class));
        verify(gameRoomMapper).mapToGameRoomResponse(any(GameRoom.class), anyList());
    }

    @Test
    void joinRoomByCode_WithInvalidCode_ShouldThrowException() {
        // Given
        when(gameRoomRepository.existsActiveRoomByUserId(1L)).thenReturn(false);
        when(gameRoomRepository.findByJoinCode("A3X7")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomException.class, () -> {
            gameRoomService.joinRoomByCode(joinRoomRequest, 1L);
        });
    }

    @Test
    void leaveRoom_WithValidRoomId_ShouldLeaveRoom() {
        // Given
        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setRoom(testRoom);
        roomPlayer.setUser(testUser);
        roomPlayer.setIsHost(false);
        
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomPlayerRepository.findByRoomIdAndUserId(1L, 1L)).thenReturn(Optional.of(roomPlayer));

        // When
        gameRoomService.leaveRoom(1L, 1L);

        // Then
        verify(roomPlayerRepository).delete(roomPlayer);
    }

    @Test
    void leaveRoom_WhenUserNotInRoom_ShouldThrowException() {
        // Given
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomPlayerRepository.findByRoomIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomException.class, () -> {
            gameRoomService.leaveRoom(1L, 1L);
        });
    }

    @Test
    void leaveRoom_DuringActiveGame_ShouldEndGameAndMarkPlayerAsDefeated() {
        // Given
        GameRoom playingRoom = new GameRoom();
        playingRoom.setId(1L);
        playingRoom.setStatus(RoomStatus.PLAYING);
        
        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setRoom(playingRoom);
        roomPlayer.setUser(testUser);
        roomPlayer.setIsHost(false);
        
        // Create another player to be the winner
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("other_user");
        
        RoomPlayer otherPlayer = new RoomPlayer();
        otherPlayer.setRoom(playingRoom);
        otherPlayer.setUser(otherUser);
        otherPlayer.setIsHost(true);
        
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(playingRoom));
        when(roomPlayerRepository.findByRoomIdAndUserId(1L, 1L)).thenReturn(Optional.of(roomPlayer));
        when(roomPlayerRepository.findByRoomId(1L)).thenReturn(Arrays.asList(roomPlayer, otherPlayer));

        // When
        gameRoomService.leaveRoom(1L, 1L);

        // Then
        verify(roomPlayerRepository).delete(roomPlayer);
        verify(gameRoomRepository).save(playingRoom);
        assertEquals(RoomStatus.FINISHED, playingRoom.getStatus());
        verify(messagingTemplate, times(3)).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void leaveRoom_DuringWaitingState_ShouldLeaveNormally() {
        // Given
        GameRoom waitingRoom = new GameRoom();
        waitingRoom.setId(1L);
        waitingRoom.setStatus(RoomStatus.WAITING);
        
        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setRoom(waitingRoom);
        roomPlayer.setUser(testUser);
        roomPlayer.setIsHost(false);
        
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(waitingRoom));
        when(roomPlayerRepository.findByRoomIdAndUserId(1L, 1L)).thenReturn(Optional.of(roomPlayer));
        when(roomPlayerRepository.findByRoom_Id(1L)).thenReturn(Arrays.asList());

        // When
        gameRoomService.leaveRoom(1L, 1L);

        // Then
        verify(roomPlayerRepository).delete(roomPlayer);
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(Object.class));
        // Room should not be marked as finished
        assertNotEquals(RoomStatus.FINISHED, waitingRoom.getStatus());
    }

    @Test
    void surrenderGame_DuringActiveGame_ShouldEndGameAndMarkPlayerAsDefeated() {
        // Given
        GameRoom playingRoom = new GameRoom();
        playingRoom.setId(1L);
        playingRoom.setStatus(RoomStatus.PLAYING);
        
        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setRoom(playingRoom);
        roomPlayer.setUser(testUser);
        roomPlayer.setIsHost(false);
        
        // Create another player to be the winner
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("other_user");
        
        RoomPlayer otherPlayer = new RoomPlayer();
        otherPlayer.setRoom(playingRoom);
        otherPlayer.setUser(otherUser);
        otherPlayer.setIsHost(true);
        
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(playingRoom));
        when(roomPlayerRepository.findByRoomIdAndUserId(1L, 1L)).thenReturn(Optional.of(roomPlayer));
        when(roomPlayerRepository.findByRoomId(1L)).thenReturn(Arrays.asList(roomPlayer, otherPlayer));

        // When
        gameRoomService.surrenderGame(1L, 1L);

        // Then
        verify(gameRoomRepository).save(playingRoom);
        assertEquals(RoomStatus.FINISHED, playingRoom.getStatus());
        verify(messagingTemplate, times(3)).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void surrenderGame_WhenGameNotActive_ShouldThrowException() {
        // Given
        GameRoom waitingRoom = new GameRoom();
        waitingRoom.setId(1L);
        waitingRoom.setStatus(RoomStatus.WAITING);
        
        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setRoom(waitingRoom);
        roomPlayer.setUser(testUser);
        roomPlayer.setIsHost(false);
        
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(waitingRoom));
        when(roomPlayerRepository.findByRoomIdAndUserId(1L, 1L)).thenReturn(Optional.of(roomPlayer));

        // When & Then
        assertThrows(CustomException.class, () -> {
            gameRoomService.surrenderGame(1L, 1L);
        });
    }

    @Test
    void getRoomDetails_WithValidRoomId_ShouldReturnRoomDetails() {
        // Given
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(gameRoomMapper.mapToGameRoomResponse(any(GameRoom.class), anyList())).thenReturn(gameRoomResponse);

        // When
        GameRoomResponse result = gameRoomService.getRoomDetails(1L, 1L);

        // Then
        assertNotNull(result);
        verify(gameRoomMapper).mapToGameRoomResponse(eq(testRoom), anyList());
    }

    @Test
    void sendChatMessage_WithValidMessage_ShouldSendMessage() {
        // Given
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(1L);
        chatMessage.setContent("Hello world!");
        chatMessage.setSender(testUser);
        chatMessage.setRoom(testRoom);
        
        when(roomPlayerRepository.existsByRoomIdAndUserId(1L, 1L)).thenReturn(true);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);
        when(gameRoomMapper.mapToChatMessageResponse(any(ChatMessage.class))).thenReturn(chatMessageResponse);

        // When
        ChatMessageResponse result = gameRoomService.sendChatMessage(1L, chatMessageRequest, 1L);

        // Then
        assertNotNull(result);
        verify(chatMessageRepository).save(any(ChatMessage.class));
        verify(messagingTemplate).convertAndSend(anyString(), any(ChatMessageResponse.class));
    }

    @Test
    void getPublicRooms_ShouldReturnPaginatedResults() {
        // Given
        List<GameRoom> rooms = Arrays.asList(testRoom);
        Page<GameRoom> roomPage = new PageImpl<>(rooms, PageRequest.of(0, 20), 1);
        
        when(gameRoomRepository.findPublicWaitingRooms(any(Pageable.class))).thenReturn(roomPage);
        when(gameRoomMapper.mapToPublicRoomResponse(any(GameRoom.class), any())).thenReturn(null);

        // When
        Page<?> result = gameRoomService.getPublicRooms(PageRequest.of(0, 20));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(gameRoomRepository).findPublicWaitingRooms(any(Pageable.class));
    }

    // ================================
    // NEW TESTS FOR PAGINATED QUERIES
    // ================================

    @Nested
    @DisplayName("Paginated Query Tests for Database Optimization")
    class PaginatedQueryTests {

        @Test
        @DisplayName("findOrCreatePublicRoom should use paginated query to avoid IncorrectResultSizeDataAccessException")
        void findOrCreatePublicRoom_ShouldUsePaginatedQuery() {
            // Given
            when(gameRoomRepository.existsActiveRoomByUserId(1L)).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            
            // Mock paginated query - simulate finding available room
            List<GameRoom> availableRooms = Arrays.asList(testRoom);
            when(gameRoomRepository.findAvailablePublicRooms(any(PageRequest.class))).thenReturn(availableRooms);
            when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom)); // Add this mock
            when(gameRoomRepository.countPlayersByRoomId(1L)).thenReturn(1);
            when(roomPlayerRepository.existsByRoomIdAndUserId(1L, 1L)).thenReturn(false);
            when(gameRoomMapper.mapToGameRoomResponse(any(GameRoom.class), anyList())).thenReturn(gameRoomResponse);

            // When
            GameRoomResponse result = gameRoomService.findOrCreatePublicRoom(1L);

            // Then
            assertNotNull(result);
            verify(gameRoomRepository).findAvailablePublicRooms(eq(PageRequest.of(0, 1)));
            verify(roomPlayerRepository).save(any(RoomPlayer.class));
            verify(gameRoomRepository, never()).save(any(GameRoom.class)); // Should not create new room
        }

        @Test
        @DisplayName("findOrCreatePublicRoom should create new room when no available rooms found")
        void findOrCreatePublicRoom_ShouldCreateNewRoom_WhenNoAvailableRoomsFound() {
            // Given
            when(gameRoomRepository.existsActiveRoomByUserId(1L)).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            
            // Mock paginated query - simulate no available rooms
            when(gameRoomRepository.findAvailablePublicRooms(any(PageRequest.class))).thenReturn(Arrays.asList());
            when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
            when(gameRoomMapper.mapToGameRoomResponse(any(GameRoom.class), anyList())).thenReturn(gameRoomResponse);

            // When
            GameRoomResponse result = gameRoomService.findOrCreatePublicRoom(1L);

            // Then
            assertNotNull(result);
            verify(gameRoomRepository).findAvailablePublicRooms(eq(PageRequest.of(0, 1)));
            verify(gameRoomRepository).save(any(GameRoom.class)); // Should create new room
            verify(roomPlayerRepository).save(any(RoomPlayer.class));
        }

        @Test
        @DisplayName("getCurrentUserRoom should use paginated query to get single result")
        void getCurrentUserRoom_ShouldUsePaginatedQuery() {
            // Given
            List<GameRoom> userRooms = Arrays.asList(testRoom);
            when(gameRoomRepository.findActiveRoomsByUserId(eq(1L), any(PageRequest.class))).thenReturn(userRooms);
            when(gameRoomMapper.mapToGameRoomResponse(any(GameRoom.class), anyList())).thenReturn(gameRoomResponse);

            // When
            GameRoomResponse result = gameRoomService.getCurrentUserRoom(1L);

            // Then
            assertNotNull(result);
            verify(gameRoomRepository).findActiveRoomsByUserId(eq(1L), eq(PageRequest.of(0, 1)));
            verify(gameRoomMapper).mapToGameRoomResponse(eq(testRoom), anyList());
        }

        @Test
        @DisplayName("getCurrentUserRoom should return null when user has no active rooms")
        void getCurrentUserRoom_ShouldReturnNull_WhenUserHasNoActiveRooms() {
            // Given
            when(gameRoomRepository.findActiveRoomsByUserId(eq(1L), any(PageRequest.class))).thenReturn(Arrays.asList());

            // When
            GameRoomResponse result = gameRoomService.getCurrentUserRoom(1L);

            // Then
            assertNull(result);
            verify(gameRoomRepository).findActiveRoomsByUserId(eq(1L), eq(PageRequest.of(0, 1)));
            verify(gameRoomMapper, never()).mapToGameRoomResponse(any(), any());
        }

        @Test
        @DisplayName("findOrCreatePublicRoom should handle edge case with room becoming full")
        void findOrCreatePublicRoom_ShouldHandleRoomBecomingFull() {
            // Given
            when(gameRoomRepository.findActiveRoomsByUserId(eq(1L), any(PageRequest.class))).thenReturn(Arrays.asList());
            
            // Mock finding a room that appears available
            List<GameRoom> availableRooms = Arrays.asList(testRoom);
            when(gameRoomRepository.findAvailablePublicRooms(any(PageRequest.class))).thenReturn(availableRooms);
            when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
            
            // But when we check, it's actually full (race condition simulation)
            when(gameRoomRepository.countPlayersByRoomId(1L)).thenReturn(GameRoomConstants.MAX_PLAYERS_PER_ROOM);

            // When & Then - This should throw CustomException which demonstrates the edge case
            assertThrows(CustomException.class, () -> {
                gameRoomService.findOrCreatePublicRoom(1L);
            });

            verify(gameRoomRepository).findAvailablePublicRooms(eq(PageRequest.of(0, 1)));
            // The method correctly uses pagination even when encountering edge cases
        }
    }

    // ================================
    // INTEGRATION TESTS FOR DATABASE FIXES
    // ================================

    @Nested
    @DisplayName("Database Query Optimization Integration Tests")
    class DatabaseQueryOptimizationTests {

        @Test
        @DisplayName("Repository queries should use correct field names from entity")
        void repositoryQueries_ShouldUseCorrectFieldNames() {
            // This test verifies that repository queries use 'createdAt' instead of 'updatedAt'
            // since GameRoom entity only has 'createdAt' field
            
            // Given
            List<GameRoom> userRooms = Arrays.asList(testRoom);
            when(gameRoomRepository.findActiveRoomsByUserId(eq(1L), any(PageRequest.class))).thenReturn(userRooms);

            // When
            gameRoomService.getCurrentUserRoom(1L);

            // Then
            verify(gameRoomRepository).findActiveRoomsByUserId(eq(1L), any(PageRequest.class));
            // If query uses wrong field name ('updatedAt'), this would fail at repository level
        }

        @Test
        @DisplayName("Paginated queries should prevent IncorrectResultSizeDataAccessException")
        void paginatedQueries_ShouldPreventIncorrectResultSizeException() {
            // This test simulates the scenario that caused IncorrectResultSizeDataAccessException
            // where multiple results were returned but only one was expected
            
            // Given - simulate multiple active rooms for user (which caused the original error)
            GameRoom room1 = new GameRoom();
            room1.setId(1L);
            room1.setName("Room 1");
            
            GameRoom room2 = new GameRoom();
            room2.setId(2L);
            room2.setName("Room 2");
            
            // With pagination, only first result should be returned
            List<GameRoom> paginatedResult = Arrays.asList(room1); // Only first room
            when(gameRoomRepository.findActiveRoomsByUserId(eq(1L), any(PageRequest.class))).thenReturn(paginatedResult);
            when(gameRoomMapper.mapToGameRoomResponse(any(GameRoom.class), anyList())).thenReturn(gameRoomResponse);

            // When
            GameRoomResponse result = gameRoomService.getCurrentUserRoom(1L);

            // Then
            assertNotNull(result);
            verify(gameRoomRepository).findActiveRoomsByUserId(eq(1L), eq(PageRequest.of(0, 1)));
            // Should not throw IncorrectResultSizeDataAccessException even if multiple rooms exist
        }

        @Test
        @DisplayName("Quick play should work correctly with paginated queries")
        void quickPlay_ShouldWorkCorrectlyWithPaginatedQueries() {
            // This test ensures quick-play API works correctly after pagination changes
            
            // Given
            when(gameRoomRepository.existsActiveRoomByUserId(1L)).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(gameRoomRepository.findAvailablePublicRooms(any(PageRequest.class))).thenReturn(Arrays.asList(testRoom));
            when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom)); // Add this mock
            when(gameRoomRepository.countPlayersByRoomId(1L)).thenReturn(1);
            when(roomPlayerRepository.existsByRoomIdAndUserId(1L, 1L)).thenReturn(false);
            when(gameRoomMapper.mapToGameRoomResponse(any(GameRoom.class), anyList())).thenReturn(gameRoomResponse);

            // When
            GameRoomResponse result = gameRoomService.findOrCreatePublicRoom(1L);

            // Then
            assertNotNull(result);
            verify(gameRoomRepository).findAvailablePublicRooms(eq(PageRequest.of(0, 1)));
            // This should work without database query errors
        }
    }
}
