package com.vn.caro_game.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import com.vn.caro_game.dtos.request.CreateRoomRequest;
import com.vn.caro_game.dtos.response.GameRoomResponse;
import com.vn.caro_game.dtos.response.RoomPlayerResponse;
import com.vn.caro_game.entities.*;
import com.vn.caro_game.enums.*;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.integrations.redis.RedisService;
import com.vn.caro_game.mappers.GameRoomMapper;
import com.vn.caro_game.repositories.*;
import com.vn.caro_game.services.impl.GameRoomServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Comprehensive unit tests for GameRoomService.
 * 
 * @author Caro Game Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GameRoomService Tests")
class GameRoomServiceSimpleTest {

    @Mock
    private GameRoomRepository gameRoomRepository;
    
    @Mock
    private RoomPlayerRepository roomPlayerRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private GameMatchRepository gameMatchRepository;
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    
    @Mock
    private RedisService redisService;
    
    @Mock
    private GameRoomMapper gameRoomMapper;
    
    @InjectMocks
    private GameRoomServiceImpl gameRoomService;

    private User host;
    private User player2;
    private GameRoom gameRoom;
    private RoomPlayer roomPlayerHost;
    private RoomPlayer roomPlayerGuest;
    
    @BeforeEach
    void setUp() {
        // Setup test users
        host = new User();
        host.setId(1L);
        host.setUsername("host");
        
        player2 = new User();
        player2.setId(2L);
        player2.setUsername("player2");
        
        // Setup game room
        gameRoom = new GameRoom();
        gameRoom.setId(1L);
        gameRoom.setName("Test Room");
        gameRoom.setIsPrivate(false);
        gameRoom.setGameState(GameState.WAITING_FOR_PLAYERS);
        gameRoom.setStatus(RoomStatus.WAITING);
        gameRoom.setCreatedBy(host);
        gameRoom.setCreatedAt(LocalDateTime.now());
        gameRoom.setRoomPlayers(new HashSet<>());
        
        // Setup room players
        roomPlayerHost = new RoomPlayer();
        roomPlayerHost.setId(new RoomPlayer.RoomPlayerId(1L, 1L));
        roomPlayerHost.setUser(host);
        roomPlayerHost.setRoom(gameRoom);
        roomPlayerHost.setIsHost(true);
        roomPlayerHost.setReadyState(PlayerReadyState.NOT_READY);
        roomPlayerHost.setJoinTime(LocalDateTime.now());
        
        roomPlayerGuest = new RoomPlayer();
        roomPlayerGuest.setId(new RoomPlayer.RoomPlayerId(1L, 2L));
        roomPlayerGuest.setUser(player2);
        roomPlayerGuest.setRoom(gameRoom);
        roomPlayerGuest.setIsHost(false);
        roomPlayerGuest.setReadyState(PlayerReadyState.NOT_READY);
        roomPlayerGuest.setJoinTime(LocalDateTime.now());
        
        // Mock default behaviors
        lenient().when(redisService.isUserOnline(anyLong())).thenReturn(true);
        lenient().when(gameRoomMapper.mapToRoomPlayerResponse(any(RoomPlayer.class), anyBoolean())).thenReturn(mock(RoomPlayerResponse.class));
        lenient().when(gameRoomMapper.mapToGameRoomResponse(any(GameRoom.class), anyList())).thenReturn(mock(GameRoomResponse.class));
        
        // Mock room player lookups
        lenient().when(roomPlayerRepository.findByRoomIdAndUserId(1L, 1L)).thenReturn(Optional.of(roomPlayerHost));
        lenient().when(roomPlayerRepository.findByRoomIdAndUserId(1L, 2L)).thenReturn(Optional.of(roomPlayerGuest));
        lenient().when(roomPlayerRepository.findByRoomIdAndUserId(1L, 3L)).thenReturn(Optional.empty());
    }
    
    @Nested
    @DisplayName("createRoom() Tests")
    class CreateRoomTests {
        
        @Test
        @DisplayName("Should successfully create a public room")
        void shouldCreatePublicRoom() {
            // Given
            CreateRoomRequest request = new CreateRoomRequest();
            request.setName("Test Room");
            request.setIsPrivate(false);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(host));
            when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(gameRoom);
            when(roomPlayerRepository.save(any(RoomPlayer.class))).thenReturn(roomPlayerHost);
            when(roomPlayerRepository.findByRoom_Id(1L)).thenReturn(Arrays.asList(roomPlayerHost));
            
            // When
            GameRoomResponse result = gameRoomService.createRoom(request, 1L);
            
            // Then
            assertNotNull(result);
            verify(gameRoomRepository).save(any(GameRoom.class));
            verify(roomPlayerRepository).save(any(RoomPlayer.class));
        }
        
        @Test
        @DisplayName("Should reject room creation with non-existent user")
        void shouldRejectRoomCreationWithNonExistentUser() {
            // Given
            CreateRoomRequest request = new CreateRoomRequest();
            request.setName("Test Room");
            request.setIsPrivate(false);
            
            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            
            // When & Then
            CustomException exception = assertThrows(CustomException.class, 
                () -> gameRoomService.createRoom(request, 1L));
            
            assertEquals(StatusCode.USER_NOT_FOUND, exception.getStatusCode());
            verify(gameRoomRepository, never()).save(any(GameRoom.class));
        }
    }
    
    @Nested
    @DisplayName("joinRoom() Tests")
    class JoinRoomTests {
        
        @Test
        @DisplayName("Should successfully join a public room")
        void shouldJoinPublicRoom() {
            // Given
            lenient().when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
            lenient().when(userRepository.findById(2L)).thenReturn(Optional.of(player2));
            lenient().when(roomPlayerRepository.findByRoomIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
            lenient().when(roomPlayerRepository.findByRoom_Id(1L)).thenReturn(Arrays.asList(roomPlayerHost));
            lenient().when(roomPlayerRepository.save(any(RoomPlayer.class))).thenReturn(roomPlayerGuest);
            
            // When
            GameRoomResponse result = gameRoomService.joinRoom(1L, 2L);
            
            // Then
            assertNotNull(result);
            verify(roomPlayerRepository).save(any(RoomPlayer.class));
        }
    }
    
    @Nested
    @DisplayName("markPlayerReady() Tests")
    class MarkPlayerReadyTests {
        
        @Test
        @DisplayName("Should mark player ready and auto-start when both ready")
        void shouldMarkPlayerReadyAndAutoStart() {
            // Given
            gameRoom.setGameState(GameState.WAITING_FOR_READY);
            gameRoom.getRoomPlayers().add(roomPlayerHost);
            gameRoom.getRoomPlayers().add(roomPlayerGuest);
            roomPlayerHost.setReadyState(PlayerReadyState.READY);
            
            when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
            when(gameRoomRepository.countPlayersByRoomId(1L)).thenReturn(2); // Mock 2 players
            when(roomPlayerRepository.save(any(RoomPlayer.class))).thenReturn(roomPlayerGuest);
            
            // When
            gameRoomService.markPlayerReady(1L, 2L);
            
            // Then
            verify(roomPlayerRepository, atLeastOnce()).save(any(RoomPlayer.class));
            verify(gameRoomRepository).save(gameRoom);
            assertEquals(GameState.IN_PROGRESS, gameRoom.getGameState());
            assertEquals(RoomStatus.PLAYING, gameRoom.getStatus());
        }
        
        @Test
        @DisplayName("Should reject ready for non-existent player")
        void shouldRejectReadyForNonExistentPlayer() {
            // Given
            lenient().when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
            lenient().when(roomPlayerRepository.findByRoomIdAndUserId(1L, 3L)).thenReturn(Optional.empty());
            
            // When & Then
            CustomException exception = assertThrows(CustomException.class, 
                () -> gameRoomService.markPlayerReady(1L, 3L));
            
            assertEquals(StatusCode.NOT_IN_ROOM, exception.getStatusCode());
            verify(roomPlayerRepository, never()).save(any(RoomPlayer.class));
        }
    }
    
    @Nested
    @DisplayName("leaveRoom() Tests")
    class LeaveRoomTests {
        
        @Test
        @DisplayName("Should allow guest to leave room")
        void shouldAllowGuestToLeaveRoom() {
            // Given
            when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
            when(roomPlayerRepository.findByRoom_Id(1L)).thenReturn(Arrays.asList(roomPlayerHost));
            
            // When
            gameRoomService.leaveRoom(1L, 2L);
            
            // Then
            verify(roomPlayerRepository).delete(roomPlayerGuest);
        }
        
        @Test
        @DisplayName("Should reject leave attempt for non-existent player")
        void shouldRejectLeaveAttemptForNonExistentPlayer() {
            // Given
            when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
            when(roomPlayerRepository.findByRoomIdAndUserId(1L, 3L)).thenReturn(Optional.empty());
            
            // When & Then
            CustomException exception = assertThrows(CustomException.class, 
                () -> gameRoomService.leaveRoom(1L, 3L));
            
            assertEquals(StatusCode.NOT_ROOM_MEMBER, exception.getStatusCode());
            verify(roomPlayerRepository, never()).delete(any(RoomPlayer.class));
        }
    }
}
