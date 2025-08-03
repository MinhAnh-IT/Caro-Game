package com.vn.caro_game.services;

import com.vn.caro_game.entities.*;
import com.vn.caro_game.enums.*;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.mappers.GameRoomMapper;
import com.vn.caro_game.repositories.*;
import com.vn.caro_game.services.impl.GameRoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Game Room Insufficient Players Logic Tests")
public class GameRoomInsufficientPlayersTest {

    @Mock
    private GameRoomRepository gameRoomRepository;
    
    @Mock
    private RoomPlayerRepository roomPlayerRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    
    @Mock
    private GameRoomMapper gameRoomMapper;

    @InjectMocks
    private GameRoomServiceImpl gameRoomService;

    private GameRoom gameRoom;
    private User host;
    private RoomPlayer roomPlayerHost;

    @BeforeEach
    void setUp() {
        // Create test entities
        host = new User();
        host.setId(1L);
        host.setUsername("host");

        gameRoom = new GameRoom();
        gameRoom.setId(1L);
        gameRoom.setGameState(GameState.WAITING_FOR_READY);
        gameRoom.setStatus(RoomStatus.WAITING);

        roomPlayerHost = new RoomPlayer();
        roomPlayerHost.setRoom(gameRoom);
        roomPlayerHost.setUser(host);
        roomPlayerHost.setIsHost(true);
        roomPlayerHost.setReadyState(PlayerReadyState.READY);
        
        // Setup composite ID
        RoomPlayer.RoomPlayerId hostId = new RoomPlayer.RoomPlayerId(1L, 1L);
        roomPlayerHost.setId(hostId);
        
        gameRoom.getRoomPlayers().add(roomPlayerHost);
    }

    @Test
    @DisplayName("Should NOT start game when only 1 player ready")
    void shouldNotStartGameWithOnlyOnePlayer() {
        // Given: Only 1 player in room
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
        when(gameRoomRepository.countPlayersByRoomId(1L)).thenReturn(1); // Only 1 player
        when(roomPlayerRepository.save(any(RoomPlayer.class))).thenReturn(roomPlayerHost);
        
        // When: Mark the only player as ready
        gameRoomService.markPlayerReady(1L, 1L);
        
        // Then: Game should NOT auto-start
        verify(gameRoomRepository, never()).save(gameRoom); // Game doesn't start, so room not saved
        assertEquals(GameState.WAITING_FOR_READY, gameRoom.getGameState());
        assertEquals(RoomStatus.WAITING, gameRoom.getStatus());
    }

    @Test
    @DisplayName("Should reject manual start when insufficient players")
    void shouldRejectManualStartWithInsufficientPlayers() {
        // Given: Only 1 player in room, trying to start manually
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
        when(gameRoomRepository.countPlayersByRoomId(1L)).thenReturn(1); // Only 1 player
        when(roomPlayerRepository.findHostByRoomId(1L)).thenReturn(Optional.of(roomPlayerHost));
        
        // When & Then: Manual start should fail
        CustomException exception = assertThrows(CustomException.class, 
            () -> gameRoomService.startGame(1L, 1L));
        
        assertEquals(StatusCode.INSUFFICIENT_PLAYERS, exception.getStatusCode());
        assertEquals("Insufficient players to start the game", exception.getMessage());
        
        // Verify game state unchanged
        assertEquals(GameState.WAITING_FOR_READY, gameRoom.getGameState());
        assertEquals(RoomStatus.WAITING, gameRoom.getStatus());
        verify(gameRoomRepository, never()).save(gameRoom);
    }

    @Test
    @DisplayName("Should auto-start game when exactly 2 players ready")
    void shouldAutoStartGameWithExactlyTwoPlayers() {
        // Given: Setup second player
        User guest = new User();
        guest.setId(2L);
        guest.setUsername("guest");
        
        RoomPlayer roomPlayerGuest = new RoomPlayer();
        roomPlayerGuest.setRoom(gameRoom);
        roomPlayerGuest.setUser(guest);
        roomPlayerGuest.setIsHost(false);
        roomPlayerGuest.setReadyState(PlayerReadyState.NOT_READY);
        
        // Setup composite ID for guest
        RoomPlayer.RoomPlayerId guestId = new RoomPlayer.RoomPlayerId(1L, 2L);
        roomPlayerGuest.setId(guestId);
        
        gameRoom.getRoomPlayers().add(roomPlayerGuest);
        
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
        when(gameRoomRepository.countPlayersByRoomId(1L)).thenReturn(2); // Exactly 2 players
        when(roomPlayerRepository.save(any(RoomPlayer.class))).thenReturn(roomPlayerGuest);
        
        // When: Mark second player as ready (first already ready)
        gameRoomService.markPlayerReady(1L, 2L);
        
        // Then: Game should auto-start
        verify(gameRoomRepository).save(gameRoom); // Game starts, so room is saved
        assertEquals(GameState.IN_PROGRESS, gameRoom.getGameState());
        assertEquals(RoomStatus.PLAYING, gameRoom.getStatus());
    }

    @Test
    @DisplayName("Should allow manual start when exactly 2 players")
    void shouldAllowManualStartWithTwoPlayers() {
        // Given: Setup second player
        User guest = new User();
        guest.setId(2L);
        guest.setUsername("guest");
        
        RoomPlayer roomPlayerGuest = new RoomPlayer();
        roomPlayerGuest.setRoom(gameRoom);
        roomPlayerGuest.setUser(guest);
        roomPlayerGuest.setIsHost(false);
        roomPlayerGuest.setReadyState(PlayerReadyState.READY);
        
        // Setup composite ID for guest
        RoomPlayer.RoomPlayerId guestId = new RoomPlayer.RoomPlayerId(1L, 2L);
        roomPlayerGuest.setId(guestId);
        
        gameRoom.getRoomPlayers().add(roomPlayerGuest);
        
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
        when(gameRoomRepository.countPlayersByRoomId(1L)).thenReturn(2); // Exactly 2 players
        when(roomPlayerRepository.findHostByRoomId(1L)).thenReturn(Optional.of(roomPlayerHost));
        when(gameRoomMapper.mapToGameRoomResponse(any(), any())).thenReturn(null); // Don't care about response
        
        // When: Manual start
        gameRoomService.startGame(1L, 1L);
        
        // Then: Game should start successfully
        verify(gameRoomRepository).save(gameRoom);
        assertEquals(RoomStatus.PLAYING, gameRoom.getStatus());
        // Note: startGame() only changes RoomStatus, not GameState
    }
}
