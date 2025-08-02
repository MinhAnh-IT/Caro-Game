package com.vn.caro_game.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.vn.caro_game.dtos.request.*;
import com.vn.caro_game.dtos.response.*;
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
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Comprehensive unit tests for GameRoomService - covering all missing functionality.
 * 
 * @author Caro Game Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GameRoomService Comprehensive Tests")
class GameRoomServiceComprehensiveTest {

    @Mock
    private GameRoomRepository gameRoomRepository;
    
    @Mock
    private RoomPlayerRepository roomPlayerRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ChatMessageRepository chatMessageRepository;
    
    @Mock
    private FriendRepository friendRepository;
    
    @Mock
    private GameHistoryRepository gameHistoryRepository;
    
    @Mock
    private GameRoomMapper gameRoomMapper;
    
    @Mock
    private RedisService redisService;
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private GameRoomServiceImpl gameRoomService;

    private User testUser;
    private User testUser2;
    private GameRoom testRoom;
    private RoomPlayer testRoomPlayer;

    @BeforeEach
    void setUp() {
        // Setup test users
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setDisplayName("Test User");

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setUsername("testuser2");
        testUser2.setDisplayName("Test User 2");

        // Setup test room
        testRoom = new GameRoom();
        testRoom.setId(1L);
        testRoom.setName("Test Room");
        testRoom.setIsPrivate(false);
        testRoom.setStatus(RoomStatus.WAITING);
        testRoom.setGameState(GameState.WAITING_FOR_PLAYERS);
        testRoom.setCreatedBy(testUser);

        // Setup test room player
        testRoomPlayer = new RoomPlayer();
        testRoomPlayer.setId(new RoomPlayer.RoomPlayerId(1L, 1L));
        testRoomPlayer.setRoom(testRoom);
        testRoomPlayer.setUser(testUser);
        testRoomPlayer.setIsHost(true);
        testRoomPlayer.setReadyState(PlayerReadyState.NOT_READY);
    }

    @Nested
    @DisplayName("Join Room By Code Tests")
    class JoinRoomByCodeTests {

        @Test
        void shouldJoinPrivateRoomWithValidCode() {
            // Given
            String joinCode = "ABCD";
            JoinRoomRequest request = new JoinRoomRequest();
            request.setJoinCode(joinCode);

            GameRoom privateRoom = new GameRoom();
            privateRoom.setId(2L);
            privateRoom.setName("Private Room");
            privateRoom.setIsPrivate(true);
            privateRoom.setStatus(RoomStatus.WAITING);
            privateRoom.setJoinCode(joinCode);

            when(gameRoomRepository.existsActiveRoomByUserId(testUser2.getId())).thenReturn(false);
            lenient().when(gameRoomRepository.findByJoinCode(joinCode)).thenReturn(Optional.of(privateRoom));
            lenient().when(gameRoomRepository.countPlayersByRoomId(privateRoom.getId())).thenReturn(1);
            lenient().when(userRepository.findById(testUser2.getId())).thenReturn(Optional.of(testUser2));
            lenient().when(roomPlayerRepository.existsByRoomIdAndUserId(privateRoom.getId(), testUser2.getId())).thenReturn(false);
            lenient().when(roomPlayerRepository.findByRoom_Id(privateRoom.getId())).thenReturn(Collections.emptyList());
            lenient().when(redisService.isUserOnline(any())).thenReturn(true);
            lenient().when(gameRoomMapper.mapToGameRoomResponse(any(), any())).thenReturn(mock(GameRoomResponse.class));

            // When
            GameRoomResponse result = gameRoomService.joinRoomByCode(request, testUser2.getId());

            // Then
            assertNotNull(result);
            verify(roomPlayerRepository).save(any(RoomPlayer.class));
            verify(messagingTemplate, atLeast(1)).convertAndSend(anyString(), any(Object.class));
        }

        @Test
        void shouldRejectInvalidJoinCode() {
            // Given
            String invalidCode = "INVALID";
            JoinRoomRequest request = new JoinRoomRequest();
            request.setJoinCode(invalidCode);

            when(gameRoomRepository.existsActiveRoomByUserId(testUser2.getId())).thenReturn(false);
            when(gameRoomRepository.findByJoinCode(invalidCode)).thenReturn(Optional.empty());

            // When & Then
            CustomException exception = assertThrows(CustomException.class, 
                () -> gameRoomService.joinRoomByCode(request, testUser2.getId()));
            assertEquals(StatusCode.INVALID_JOIN_CODE, exception.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Find Or Create Public Room Tests")
    class FindOrCreatePublicRoomTests {

        @Test
        void shouldReturnExistingRoomIfUserAlreadyInRoom() {
            // Given
            List<GameRoom> activeRooms = List.of(testRoom);
            when(gameRoomRepository.findActiveRoomsByUserId(eq(testUser.getId()), any(PageRequest.class)))
                .thenReturn(activeRooms);
            when(roomPlayerRepository.findByRoom_Id(testRoom.getId())).thenReturn(Collections.emptyList());
            when(gameRoomMapper.mapToGameRoomResponse(any(), any())).thenReturn(mock(GameRoomResponse.class));

            // When
            GameRoomResponse result = gameRoomService.findOrCreatePublicRoom(testUser.getId());

            // Then
            assertNotNull(result);
            verify(gameRoomRepository, never()).save(any(GameRoom.class));
        }

        @Test
        void shouldJoinAvailablePublicRoom() {
            // Given
            GameRoom availableRoom = new GameRoom();
            availableRoom.setId(3L);
            availableRoom.setIsPrivate(false);
            availableRoom.setStatus(RoomStatus.WAITING);

            when(gameRoomRepository.findActiveRoomsByUserId(eq(testUser.getId()), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
            lenient().when(gameRoomRepository.findAvailablePublicRooms(any(PageRequest.class)))
                .thenReturn(List.of(availableRoom));
            lenient().when(gameRoomRepository.findById(availableRoom.getId())).thenReturn(Optional.of(availableRoom));
            lenient().when(gameRoomRepository.countPlayersByRoomId(availableRoom.getId())).thenReturn(1);
            lenient().when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            lenient().when(roomPlayerRepository.existsByRoomIdAndUserId(availableRoom.getId(), testUser.getId())).thenReturn(false);
            lenient().when(roomPlayerRepository.findByRoom_Id(availableRoom.getId())).thenReturn(Collections.emptyList());
            lenient().when(redisService.isUserOnline(any())).thenReturn(true);
            lenient().when(gameRoomMapper.mapToGameRoomResponse(any(), any())).thenReturn(mock(GameRoomResponse.class));

            // When
            GameRoomResponse result = gameRoomService.findOrCreatePublicRoom(testUser.getId());

            // Then
            assertNotNull(result);
            verify(roomPlayerRepository).save(any(RoomPlayer.class));
        }

        @Test
        void shouldCreateNewPublicRoomIfNoneAvailable() {
            // Given
            when(gameRoomRepository.findActiveRoomsByUserId(eq(testUser.getId()), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
            when(gameRoomRepository.findAvailablePublicRooms(any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
            when(gameRoomRepository.existsActiveRoomByUserId(testUser.getId())).thenReturn(false);
            lenient().when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            lenient().when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
            lenient().when(roomPlayerRepository.findByRoom_Id(testRoom.getId())).thenReturn(Collections.emptyList());
            lenient().when(redisService.isUserOnline(any())).thenReturn(true);
            lenient().when(gameRoomMapper.mapToGameRoomResponse(any(), any())).thenReturn(mock(GameRoomResponse.class));

            // When
            GameRoomResponse result = gameRoomService.findOrCreatePublicRoom(testUser.getId());

            // Then
            assertNotNull(result);
            verify(gameRoomRepository).save(any(GameRoom.class));
            verify(roomPlayerRepository).save(any(RoomPlayer.class));
        }
    }

    @Nested
    @DisplayName("Surrender Game Tests")
    class SurrenderGameTests {

        @Test
        void shouldSurrenderDuringActiveGame() {
            // Given
            testRoom.setStatus(RoomStatus.PLAYING);
            List<RoomPlayer> players = List.of(testRoomPlayer, createSecondPlayer());

            when(gameRoomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(roomPlayerRepository.findByRoomIdAndUserId(testRoom.getId(), testUser.getId()))
                .thenReturn(Optional.of(testRoomPlayer));
            when(roomPlayerRepository.findByRoomId(testRoom.getId())).thenReturn(players);

            // When
            gameRoomService.surrenderGame(testRoom.getId(), testUser.getId());

            // Then
            verify(roomPlayerRepository, times(2)).save(any(RoomPlayer.class)); // Both players
            verify(gameHistoryRepository).save(any(GameHistory.class));
            verify(gameRoomRepository).save(any(GameRoom.class));
            verify(messagingTemplate, atLeast(2)).convertAndSend(anyString(), any(Object.class));
        }

        @Test
        void shouldRejectSurrenderWhenGameNotActive() {
            // Given
            testRoom.setStatus(RoomStatus.WAITING);
            when(gameRoomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(roomPlayerRepository.findByRoomIdAndUserId(testRoom.getId(), testUser.getId()))
                .thenReturn(Optional.of(testRoomPlayer));

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                () -> gameRoomService.surrenderGame(testRoom.getId(), testUser.getId()));
            assertEquals(StatusCode.GAME_NOT_ACTIVE, exception.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Complete Game Tests")
    class CompleteGameTests {

        @Test
        void shouldCompleteGameWithWinner() {
            // Given
            testRoom.setStatus(RoomStatus.PLAYING);
            Long winnerId = testUser.getId();
            Long loserId = testUser2.getId();

            when(gameRoomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(roomPlayerRepository.findByRoomIdAndUserId(testRoom.getId(), winnerId))
                .thenReturn(Optional.of(testRoomPlayer));
            when(roomPlayerRepository.findByRoomIdAndUserId(testRoom.getId(), loserId))
                .thenReturn(Optional.of(createSecondPlayer()));

            // When
            gameRoomService.completeGame(testRoom.getId(), winnerId, loserId);

            // Then
            verify(roomPlayerRepository, times(2)).save(any(RoomPlayer.class));
            verify(gameHistoryRepository).save(any(GameHistory.class));
            verify(gameRoomRepository).save(any(GameRoom.class));
            verify(messagingTemplate, atLeast(1)).convertAndSend(anyString(), any(Object.class));
        }

        @Test
        void shouldRejectCompleteWhenGameNotActive() {
            // Given
            testRoom.setStatus(RoomStatus.FINISHED);
            when(gameRoomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                () -> gameRoomService.completeGame(testRoom.getId(), testUser.getId(), testUser2.getId()));
            assertEquals(StatusCode.GAME_NOT_ACTIVE, exception.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Rematch System Tests")
    class RematchSystemTests {

        @Test
        void shouldRequestRematch() {
            // Given
            testRoom.setStatus(RoomStatus.FINISHED);
            testRoom.setRematchState(RematchState.NONE);
            testRoom.setGameState(GameState.FINISHED); // Set correct state for rematch
            
            // Add both players to the room for valid rematch
            RoomPlayer secondPlayer = createSecondPlayer();
            testRoom.setRoomPlayers(Set.of(testRoomPlayer, secondPlayer));

            when(gameRoomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(roomPlayerRepository.countPlayersByRoomId(testRoom.getId())).thenReturn(2L);
            when(roomPlayerRepository.findByRoomIdAndUserId(testRoom.getId(), testUser.getId()))
                .thenReturn(Optional.of(testRoomPlayer));

            // When
            gameRoomService.requestRematch(testRoom.getId(), testUser.getId());

            // Then
            verify(gameRoomRepository).save(any(GameRoom.class));
            verify(messagingTemplate, atLeast(1)).convertAndSend(anyString(), any(Object.class));
        }

        @Test
        void shouldAcceptRematch() {
            // Given
            testRoom.setStatus(RoomStatus.FINISHED);
            testRoom.setRematchState(RematchState.REQUESTED);
            testRoom.setRematchRequesterId(testUser2.getId());
            
            RoomPlayer secondPlayer = createSecondPlayer();
            secondPlayer.setAcceptedRematch(false);
            testRoomPlayer.setAcceptedRematch(false);
            
            testRoom.setGameState(GameState.FINISHED); // Set correct state for rematch
            testRoom.setRoomPlayers(Set.of(testRoomPlayer, secondPlayer));

            lenient().when(gameRoomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            lenient().when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            lenient().when(userRepository.findById(testUser2.getId())).thenReturn(Optional.of(testUser2));
            lenient().when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
            lenient().when(roomPlayerRepository.countPlayersByRoomId(testRoom.getId())).thenReturn(2L);
            lenient().when(roomPlayerRepository.findByRoomIdAndUserId(testRoom.getId(), testUser.getId()))
                .thenReturn(Optional.of(testRoomPlayer));

            // When
            gameRoomService.acceptRematch(testRoom.getId(), testUser.getId());

            // Then
            verify(roomPlayerRepository, atLeast(2)).save(any(RoomPlayer.class));
            verify(messagingTemplate, atLeast(1)).convertAndSend(anyString(), any(Object.class));
        }

        @Test
        void shouldRejectRematchWhenNoneRequested() {
            // Given
            testRoom.setStatus(RoomStatus.FINISHED);
            testRoom.setRematchState(RematchState.NONE);
            testRoom.setGameState(GameState.FINISHED); 
            testRoom.setRoomPlayers(Set.of(testRoomPlayer));

            when(gameRoomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(roomPlayerRepository.findByRoomIdAndUserId(testRoom.getId(), testUser.getId()))
                .thenReturn(Optional.of(testRoomPlayer));

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                () -> gameRoomService.acceptRematch(testRoom.getId(), testUser.getId()));
            assertEquals(StatusCode.NO_REMATCH_REQUEST, exception.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Start Game Tests")
    class StartGameTests {

        @Test
        void shouldStartGameAsHost() {
            // Given
            testRoom.setStatus(RoomStatus.WAITING);
            when(gameRoomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(roomPlayerRepository.findHostByRoomId(testRoom.getId()))
                .thenReturn(Optional.of(testRoomPlayer));
            when(gameRoomRepository.countPlayersByRoomId(testRoom.getId())).thenReturn(2);
            when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
            when(roomPlayerRepository.findByRoom_Id(testRoom.getId())).thenReturn(Collections.emptyList());
            when(gameRoomMapper.mapToGameRoomResponse(any(), any())).thenReturn(mock(GameRoomResponse.class));

            // When
            GameRoomResponse result = gameRoomService.startGame(testRoom.getId(), testUser.getId());

            // Then
            assertNotNull(result);
            verify(gameRoomRepository).save(any(GameRoom.class));
            verify(messagingTemplate, atLeast(1)).convertAndSend(anyString(), any(Object.class));
        }

        @Test
        void shouldRejectStartGameIfNotHost() {
            // Given
            RoomPlayer nonHost = createSecondPlayer();
            nonHost.setIsHost(false);

            when(gameRoomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(roomPlayerRepository.findHostByRoomId(testRoom.getId()))
                .thenReturn(Optional.of(testRoomPlayer));

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                () -> gameRoomService.startGame(testRoom.getId(), testUser2.getId()));
            assertEquals(StatusCode.FORBIDDEN, exception.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Room Details Tests")
    class RoomDetailsTests {

        @Test
        void shouldGetPublicRoomDetails() {
            // Given
            testRoom.setIsPrivate(false);
            when(gameRoomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(roomPlayerRepository.findByRoom_Id(testRoom.getId())).thenReturn(Collections.emptyList());
            when(gameRoomMapper.mapToGameRoomResponse(any(), any())).thenReturn(mock(GameRoomResponse.class));

            // When
            GameRoomResponse result = gameRoomService.getRoomDetails(testRoom.getId(), testUser.getId());

            // Then
            assertNotNull(result);
            verify(gameRoomMapper).mapToGameRoomResponse(any(), any());
        }

        @Test
        void shouldRejectPrivateRoomAccessForNonMember() {
            // Given
            testRoom.setIsPrivate(true);
            when(gameRoomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(roomPlayerRepository.existsByRoomIdAndUserId(testRoom.getId(), testUser2.getId()))
                .thenReturn(false);

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                () -> gameRoomService.getRoomDetails(testRoom.getId(), testUser2.getId()));
            assertEquals(StatusCode.NOT_ROOM_MEMBER, exception.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Chat System Tests")
    class ChatSystemTests {

        @Test
        void shouldSendChatMessage() {
            // Given
            SendChatMessageRequest request = new SendChatMessageRequest();
            request.setContent("Hello world!");

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setId(1L);
            chatMessage.setContent("Hello world!");
            chatMessage.setSender(testUser);
            chatMessage.setRoom(testRoom);

            when(roomPlayerRepository.existsByRoomIdAndUserId(testRoom.getId(), testUser.getId()))
                .thenReturn(true);
            when(gameRoomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);
            when(gameRoomMapper.mapToChatMessageResponse(any())).thenReturn(mock(ChatMessageResponse.class));

            // When
            ChatMessageResponse result = gameRoomService.sendChatMessage(testRoom.getId(), request, testUser.getId());

            // Then
            assertNotNull(result);
            verify(chatMessageRepository).save(any(ChatMessage.class));
            verify(messagingTemplate).convertAndSend(anyString(), any(Object.class));
        }

        @Test
        void shouldRejectChatFromNonMember() {
            // Given
            SendChatMessageRequest request = new SendChatMessageRequest();
            request.setContent("Hello world!");

            when(roomPlayerRepository.existsByRoomIdAndUserId(testRoom.getId(), testUser2.getId()))
                .thenReturn(false);

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                () -> gameRoomService.sendChatMessage(testRoom.getId(), request, testUser2.getId()));
            assertEquals(StatusCode.NOT_ROOM_MEMBER, exception.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Game History Tests")
    class GameHistoryTests {

        @Test
        void shouldGetUserGameHistory() {
            // Given
            GameHistory history = new GameHistory();
            history.setId(1L);
            history.setRoomId(testRoom.getId());
            history.setWinnerId(testUser.getId());
            history.setLoserId(testUser2.getId());
            history.setEndReason(GameEndReason.WIN);

            Pageable pageable = PageRequest.of(0, 10);
            Page<GameHistory> historyPage = new PageImpl<>(List.of(history), pageable, 1);

            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(gameHistoryRepository.findByUserId(testUser.getId(), pageable)).thenReturn(historyPage);
            when(userRepository.findById(testUser2.getId())).thenReturn(Optional.of(testUser2));

            // When
            Page<GameHistoryResponse> result = gameRoomService.getUserGameHistory(testUser.getId(), pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(gameHistoryRepository).findByUserId(testUser.getId(), pageable);
        }
    }

    @Nested
    @DisplayName("Public Rooms Tests")
    class PublicRoomsTests {

        @Test
        void shouldGetPublicRooms() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<GameRoom> roomsPage = new PageImpl<>(List.of(testRoom), pageable, 1);

            when(gameRoomRepository.findPublicWaitingRooms(pageable)).thenReturn(roomsPage);
            when(gameRoomRepository.countPlayersByRoomId(testRoom.getId())).thenReturn(1);
            when(gameRoomMapper.mapToPublicRoomResponse(any(), any())).thenReturn(mock(PublicRoomResponse.class));

            // When
            Page<PublicRoomResponse> result = gameRoomService.getPublicRooms(pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(gameRoomRepository).findPublicWaitingRooms(pageable);
        }
    }

    @Nested
    @DisplayName("User Rooms Tests")
    class UserRoomsTests {

        @Test
        void shouldGetUserRooms() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<GameRoom> roomsPage = new PageImpl<>(List.of(testRoom), pageable, 1);

            when(gameRoomRepository.findRoomsByUserIdPaged(testUser.getId(), pageable)).thenReturn(roomsPage);
            when(roomPlayerRepository.findByRoom_Id(testRoom.getId())).thenReturn(Collections.emptyList());
            when(gameRoomMapper.mapToGameRoomResponse(any(), any())).thenReturn(mock(GameRoomResponse.class));

            // When
            Page<GameRoomResponse> result = gameRoomService.getUserRooms(testUser.getId(), pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(gameRoomRepository).findRoomsByUserIdPaged(testUser.getId(), pageable);
        }
    }

    // Helper methods
    private RoomPlayer createSecondPlayer() {
        RoomPlayer secondPlayer = new RoomPlayer();
        secondPlayer.setId(new RoomPlayer.RoomPlayerId(testRoom.getId(), testUser2.getId()));
        secondPlayer.setRoom(testRoom);
        secondPlayer.setUser(testUser2);
        secondPlayer.setIsHost(false);
        secondPlayer.setReadyState(PlayerReadyState.NOT_READY);
        return secondPlayer;
    }
}
