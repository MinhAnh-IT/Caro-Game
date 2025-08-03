package com.vn.caro_game.services;

import com.vn.caro_game.dtos.response.GameReplayResponse;
import com.vn.caro_game.dtos.response.GameStatisticsResponse;
import com.vn.caro_game.dtos.response.GameHistorySummaryResponse;
import com.vn.caro_game.entities.*;
import com.vn.caro_game.enums.GameEndReason;
import com.vn.caro_game.enums.GameResult;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.repositories.*;
import com.vn.caro_game.services.impl.GameStatisticsServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;/** * Comprehensive unit tests for GameStatisticsService * Testing all statistics calculation and game replay functionalities *  * @author Caro Game Team * @since 1.0.0 */@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Game Statistics Service Tests")
class GameStatisticsServiceTest {

    @Mock
    private GameHistoryRepository gameHistoryRepository;

    @Mock
    private GameMatchRepository gameMatchRepository;

    @Mock
    private MoveRepository moveRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameRoomRepository gameRoomRepository;

    @InjectMocks
    private GameStatisticsServiceImpl gameStatisticsService;

    private User testUser1;
    private User testUser2;
    private GameRoom testRoom;
    private GameMatch testMatch;
    private GameHistory testHistory;
    private List<Move> testMoves;

    @BeforeEach
    void setUp() {
        // Setup test user 1
        testUser1 = new User();
        testUser1.setId(1L);
        testUser1.setUsername("testuser1");
        testUser1.setEmail("test1@example.com");
        testUser1.setDisplayName("Test User 1");
        testUser1.setAvatarUrl("https://example.com/avatar1.jpg");

        // Setup test user 2
        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setUsername("testuser2");
        testUser2.setEmail("test2@example.com");
        testUser2.setDisplayName("Test User 2");
        testUser2.setAvatarUrl("https://example.com/avatar2.jpg");

        // Setup test room
        testRoom = new GameRoom();
        testRoom.setId(1L);
        testRoom.setName("Test Room");
        testRoom.setCreatedBy(testUser1);

        // Setup test match with correct entity properties
        testMatch = new GameMatch();
        testMatch.setId(1L);
        testMatch.setRoom(testRoom);
        testMatch.setPlayerX(testUser1);
        testMatch.setPlayerO(testUser2);
        testMatch.setResult(GameResult.X_WIN);
        testMatch.setStartTime(LocalDateTime.now().minusMinutes(30));
        testMatch.setEndTime(LocalDateTime.now());

        // Setup test history with correct entity properties
        testHistory = new GameHistory();
        testHistory.setId(1L);
        testHistory.setRoomId(testRoom.getId());
        testHistory.setWinnerId(testUser1.getId());
        testHistory.setLoserId(testUser2.getId());
        testHistory.setEndReason(GameEndReason.WIN);
        testHistory.setGameStartedAt(LocalDateTime.now().minusMinutes(30));
        testHistory.setGameEndedAt(LocalDateTime.now());

        // Setup test moves with correct entity properties
        Move move1 = new Move();
        move1.setId(1L);
        move1.setMatch(testMatch);
        move1.setPlayer(testUser1);
        move1.setXPosition(5);
        move1.setYPosition(5);
        move1.setMoveNumber(1);
        move1.setCreatedAt(LocalDateTime.now().minusMinutes(29));

        Move move2 = new Move();
        move2.setId(2L);
        move2.setMatch(testMatch);
        move2.setPlayer(testUser2);
        move2.setXPosition(6);
        move2.setYPosition(5);
        move2.setMoveNumber(2);
        move2.setCreatedAt(LocalDateTime.now().minusMinutes(28));

        testMoves = Arrays.asList(move1, move2);
    }

    @Nested
    @DisplayName("getUserGameStatistics Tests")
    class GetUserGameStatisticsTests {

        @Test
        @Order(1)
        @DisplayName("Should calculate statistics for user with games")
        void shouldCalculateStatisticsForUserWithGames() {
            // Given
            Long userId = testUser1.getId();
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser1));
            when(gameHistoryRepository.getWinCountByUserId(userId)).thenReturn(10L);
            when(gameHistoryRepository.getLossCountByUserId(userId)).thenReturn(5L);
            when(gameHistoryRepository.findByUserIdList(userId)).thenReturn(Arrays.asList(testHistory));

            // When
            GameStatisticsResponse result = gameStatisticsService.getUserGameStatistics(userId);

            // Then
            assertNotNull(result);
            assertEquals(userId, result.getUserId());
            assertEquals(10L, result.getTotalWins());
            assertEquals(5L, result.getTotalLosses());
            assertTrue(result.getWinRate() > 0);
            verify(gameHistoryRepository).getWinCountByUserId(userId);
            verify(gameHistoryRepository).getLossCountByUserId(userId);
        }

        @Test
        @Order(2)
        @DisplayName("Should handle user with no games")
        void shouldHandleUserWithNoGames() {
            // Given
            Long userId = testUser1.getId();
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser1));
            when(gameHistoryRepository.getWinCountByUserId(userId)).thenReturn(0L);
            when(gameHistoryRepository.getLossCountByUserId(userId)).thenReturn(0L);
            when(gameHistoryRepository.findByUserIdList(userId)).thenReturn(Arrays.asList());

            // When
            GameStatisticsResponse result = gameStatisticsService.getUserGameStatistics(userId);

            // Then
            assertNotNull(result);
            assertEquals(userId, result.getUserId());
            assertEquals(0L, result.getTotalWins());
            assertEquals(0L, result.getTotalLosses());
            assertEquals(0.0, result.getWinRate());
        }

        @Test
        @Order(3)
        @DisplayName("Should throw exception for non-existent user")
        void shouldThrowExceptionForNonExistentUser() {
            // Given
            Long userId = 999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(CustomException.class, () -> 
                gameStatisticsService.getUserGameStatistics(userId));
        }
    }

    @Nested
    @DisplayName("getGameReplay Tests")
    class GetGameReplayTests {

        @Test
        @Order(4)
        @DisplayName("Should get game replay for valid user and game")
        void shouldGetGameReplayForValidUserAndGame() {
            // Given
            Long gameHistoryId = testHistory.getId();
            Long userId = testUser1.getId();
            
            when(gameHistoryRepository.findById(gameHistoryId)).thenReturn(Optional.of(testHistory));
            when(gameRoomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(gameMatchRepository.findByRoomId(testRoom.getId())).thenReturn(Arrays.asList(testMatch));
            when(moveRepository.findByMatchIdOrderByMoveNumberAsc(testMatch.getId())).thenReturn(testMoves);

            // When
            GameReplayResponse result = gameStatisticsService.getGameReplay(gameHistoryId, userId);

            // Then
            assertNotNull(result);
            assertEquals(gameHistoryId, result.getGameId());
            assertEquals(testRoom.getId(), result.getRoomId());
            assertEquals(testRoom.getName(), result.getRoomName());
            assertNotNull(result.getPlayerX());
            assertNotNull(result.getPlayerO());
            assertEquals(testUser1.getId(), result.getPlayerX().getPlayerId());
            assertEquals(testUser2.getId(), result.getPlayerO().getPlayerId());
        }

        @Test
        @Order(5)
        @DisplayName("Should throw exception for unauthorized access")
        void shouldThrowExceptionForUnauthorizedAccess() {
            // Given
            Long gameHistoryId = testHistory.getId();
            Long unauthorizedUserId = 999L;
            
            when(gameHistoryRepository.findById(gameHistoryId)).thenReturn(Optional.of(testHistory));

            // When & Then
            assertThrows(CustomException.class, () -> 
                gameStatisticsService.getGameReplay(gameHistoryId, unauthorizedUserId));
        }

        @Test
        @Order(6)
        @DisplayName("Should throw exception for non-existent game")
        void shouldThrowExceptionForNonExistentGame() {
            // Given
            Long gameHistoryId = 999L;
            Long userId = testUser1.getId();
            
            when(gameHistoryRepository.findById(gameHistoryId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(CustomException.class, () -> 
                gameStatisticsService.getGameReplay(gameHistoryId, userId));
        }
    }

    @Nested
    @DisplayName("getUserGameReplays Tests")
    class GetUserGameReplaysTests {

        @Test
        @Order(7)
        @DisplayName("Should get paginated game replays for user")
        void shouldGetPaginatedGameReplaysForUser() {
            // Given
            Long userId = testUser1.getId();
            Pageable pageable = PageRequest.of(0, 10);
            Page<GameHistory> historyPage = new PageImpl<>(Arrays.asList(testHistory), pageable, 1);
            
            when(gameHistoryRepository.findByUserId(userId, pageable)).thenReturn(historyPage);
            when(gameRoomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));

            // When
            Page<GameHistorySummaryResponse> result = gameStatisticsService.getUserGameReplays(userId, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());
            GameHistorySummaryResponse replay = result.getContent().get(0);
            assertEquals(testHistory.getId(), replay.getGameId());
            assertEquals(testRoom.getId(), replay.getRoomId());
        }

        @Test
        @Order(8)
        @DisplayName("Should return empty page for user with no games")
        void shouldReturnEmptyPageForUserWithNoGames() {
            // Given
            Long userId = testUser1.getId();
            Pageable pageable = PageRequest.of(0, 10);
            Page<GameHistory> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
            
            when(gameHistoryRepository.findByUserId(userId, pageable)).thenReturn(emptyPage);

            // When
            Page<GameHistorySummaryResponse> result = gameStatisticsService.getUserGameReplays(userId, pageable);

            // Then
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());
        }
    }

    @Nested
    @DisplayName("getTopPlayersByWinRate Tests")
    class GetTopPlayersByWinRateTests {

        @Test
        @Order(9)
        @DisplayName("Should get top players by win rate")
        void shouldGetTopPlayersByWinRate() {
            // Given
            int limit = 10;
            Pageable pageable = PageRequest.of(0, limit);
            when(userRepository.findUsersWithGameHistory()).thenReturn(Arrays.asList(testUser1, testUser2));
            
            // Mock getUserGameStatistics calls
            when(gameHistoryRepository.getWinCountByUserId(testUser1.getId())).thenReturn(10L);
            when(gameHistoryRepository.getLossCountByUserId(testUser1.getId())).thenReturn(5L);
            when(gameHistoryRepository.findByUserIdList(testUser1.getId())).thenReturn(Arrays.asList());
            when(userRepository.findById(testUser1.getId())).thenReturn(Optional.of(testUser1));
            
            when(gameHistoryRepository.getWinCountByUserId(testUser2.getId())).thenReturn(3L);
            when(gameHistoryRepository.getLossCountByUserId(testUser2.getId())).thenReturn(2L);
            when(gameHistoryRepository.findByUserIdList(testUser2.getId())).thenReturn(Arrays.asList());
            when(userRepository.findById(testUser2.getId())).thenReturn(Optional.of(testUser2));

            // When
            Page<GameStatisticsResponse> result = gameStatisticsService.getTopPlayersByWinRate(limit, pageable);

            // Then
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals(2, result.getContent().size());
            
            // Verify ordering by win rate
            GameStatisticsResponse firstUser = result.getContent().get(0);
            GameStatisticsResponse secondUser = result.getContent().get(1);
            assertTrue(firstUser.getWinRate() >= secondUser.getWinRate());
        }

        @Test
        @Order(10)
        @DisplayName("Should return empty page when no users")
        void shouldReturnEmptyPageWhenNoUsers() {
            // Given
            int limit = 10;
            Pageable pageable = PageRequest.of(0, limit);
            when(userRepository.findUsersWithGameHistory()).thenReturn(Arrays.asList());

            // When
            Page<GameStatisticsResponse> result = gameStatisticsService.getTopPlayersByWinRate(limit, pageable);

            // Then
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());
        }
    }

    @Nested
    @DisplayName("getUserRanking Tests")
    class GetUserRankingTests {

        @Test
        @Order(11)
        @DisplayName("Should get user ranking")
        void shouldGetUserRanking() {
            // Given
            Long userId = testUser1.getId();
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser1));
            when(gameHistoryRepository.getWinCountByUserId(userId)).thenReturn(10L);
            when(gameHistoryRepository.getLossCountByUserId(userId)).thenReturn(5L);

            // When
            Long result = gameStatisticsService.getUserRanking(userId);

            // Then
            assertNotNull(result);
            assertTrue(result >= 1L); // Ranking should be at least 1
        }

        @Test
        @Order(12)
        @DisplayName("Should throw exception for non-existent user ranking")
        void shouldThrowExceptionForNonExistentUserRanking() {
            // Given
            Long userId = 999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(CustomException.class, () -> 
                gameStatisticsService.getUserRanking(userId));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesAndIntegrationTests {

        @Test
        @Order(13)
        @DisplayName("Should handle games with no moves")
        void shouldHandleGamesWithNoMoves() {
            // Given
            Long gameHistoryId = testHistory.getId();
            Long userId = testUser1.getId();
            
            when(gameHistoryRepository.findById(gameHistoryId)).thenReturn(Optional.of(testHistory));
            when(gameRoomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(gameMatchRepository.findByRoomId(testRoom.getId())).thenReturn(Arrays.asList(testMatch));
            when(moveRepository.findByMatchIdOrderByMoveNumberAsc(testMatch.getId())).thenReturn(Arrays.asList());

            // When
            GameReplayResponse result = gameStatisticsService.getGameReplay(gameHistoryId, userId);

            // Then
            assertNotNull(result);
            assertEquals(gameHistoryId, result.getGameId());
            assertTrue(result.getMoves().isEmpty());
        }

        @Test
        @Order(14)
        @DisplayName("Should handle draw games in statistics")
        void shouldHandleDrawGamesInStatistics() {
            // Given
            Long userId = testUser1.getId();
            GameHistory drawHistory = new GameHistory();
            drawHistory.setId(2L);
            drawHistory.setRoomId(testRoom.getId());
            drawHistory.setWinnerId(null);
            drawHistory.setLoserId(null);
            drawHistory.setEndReason(GameEndReason.WIN);
            drawHistory.setGameStartedAt(LocalDateTime.now().minusMinutes(30));
            drawHistory.setGameEndedAt(LocalDateTime.now());

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser1));
            when(gameHistoryRepository.getWinCountByUserId(userId)).thenReturn(10L);
            when(gameHistoryRepository.getLossCountByUserId(userId)).thenReturn(5L);
            when(gameHistoryRepository.findByUserIdList(userId)).thenReturn(Arrays.asList(testHistory, drawHistory));

            // When
            GameStatisticsResponse result = gameStatisticsService.getUserGameStatistics(userId);

            // Then
            assertNotNull(result);
            assertEquals(userId, result.getUserId());
            assertEquals(10L, result.getTotalWins());
            assertEquals(5L, result.getTotalLosses());
            assertTrue(result.getTotalDraws() >= 0);
        }
    }
}
