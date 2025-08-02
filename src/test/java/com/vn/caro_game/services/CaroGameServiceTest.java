package com.vn.caro_game.services;

import com.vn.caro_game.constants.CaroGameConstants;
import com.vn.caro_game.dtos.request.GameMoveRequest;
import com.vn.caro_game.dtos.response.GameMoveResponse;
import com.vn.caro_game.entities.*;
import com.vn.caro_game.enums.*;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.repositories.*;
import com.vn.caro_game.services.impl.CaroGameServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for CaroGameService implementation.
 * Tests game logic, move validation, turn management, and win conditions.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CaroGameService Tests")
class CaroGameServiceTest {

    @Mock
    private GameRoomRepository gameRoomRepository;
    
    @Mock
    private GameMatchRepository gameMatchRepository;
    
    @Mock
    private MoveRepository moveRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    
    @Mock
    private GameHistoryRepository gameHistoryRepository;
    
    @Mock
    private RoomPlayerRepository roomPlayerRepository;
    
    @InjectMocks
    private CaroGameServiceImpl caroGameService;
    
    private GameRoom gameRoom;
    private GameMatch gameMatch;
    private User playerX;
    private User playerO;
    private RoomPlayer roomPlayerX;
    private RoomPlayer roomPlayerO;
    
    @BeforeEach
    void setUp() {
        // Setup test users
        playerX = new User();
        playerX.setId(1L);
        playerX.setUsername("playerX");
        
        playerO = new User();
        playerO.setId(2L);
        playerO.setUsername("playerO");
        
        // Setup game room
        gameRoom = new GameRoom();
        gameRoom.setId(1L);
        gameRoom.setName("Test Room");
        gameRoom.setGameState(GameState.IN_PROGRESS);
        gameRoom.setStatus(RoomStatus.PLAYING);
        gameRoom.setGameStartedAt(LocalDateTime.now());
        
        // Setup room players
        roomPlayerX = new RoomPlayer();
        roomPlayerX.setUser(playerX);
        roomPlayerX.setRoom(gameRoom);
        roomPlayerX.setIsHost(true);
        
        roomPlayerO = new RoomPlayer();
        roomPlayerO.setUser(playerO);
        roomPlayerO.setRoom(gameRoom);
        roomPlayerO.setIsHost(false);
        
        gameRoom.setRoomPlayers(new HashSet<>(Arrays.asList(roomPlayerX, roomPlayerO)));
        
        // Setup game match
        gameMatch = new GameMatch();
        gameMatch.setId(1L);
        gameMatch.setRoom(gameRoom);
        gameMatch.setPlayerX(playerX);
        gameMatch.setPlayerO(playerO);
        gameMatch.setResult(GameResult.ONGOING);
        gameMatch.setStartTime(LocalDateTime.now());
    }
    
    @Nested
    @DisplayName("makeMove() Tests")
    class MakeMoveTests {
        
        @Test
        @DisplayName("Should successfully make a valid move")
        void shouldMakeValidMove() {
            // Given
            GameMoveRequest request = new GameMoveRequest();
            request.setXPosition(0);
            request.setYPosition(0);
            
            when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
            when(gameMatchRepository.findByRoomAndResult(gameRoom, GameResult.ONGOING))
                .thenReturn(Optional.of(gameMatch));
            when(moveRepository.countByMatch(gameMatch)).thenReturn(0); // X's turn
            when(userRepository.findById(1L)).thenReturn(Optional.of(playerX));
            
            Move savedMove = new Move();
            savedMove.setId(1L);
            savedMove.setXPosition(0);
            savedMove.setYPosition(0);
            savedMove.setMoveNumber(1);
            savedMove.setPlayer(playerX);
            savedMove.setMatch(gameMatch);
            when(moveRepository.save(any(Move.class))).thenReturn(savedMove);
            
            // When
            GameMoveResponse response = caroGameService.makeMove(1L, request, 1L);
            
            // Then
            assertNotNull(response);
            assertEquals(0, response.getXPosition());
            assertEquals(0, response.getYPosition());
            assertEquals(1L, response.getPlayerId());
            assertEquals("X", response.getPlayerSymbol());
            assertEquals(2L, response.getNextTurnPlayerId()); // Next turn is O
            assertTrue(response.getIsValidMove());
            
            verify(moveRepository).save(any(Move.class));
            verify(messagingTemplate).convertAndSend(anyString(), eq(response));
        }
        
        @Test
        @DisplayName("Should reject move on occupied cell")
        void shouldRejectMoveOnOccupiedCell() {
            // Given
            GameMoveRequest request = new GameMoveRequest();
            request.setXPosition(0);
            request.setYPosition(0);
            
            when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
            when(gameMatchRepository.findByRoomAndResult(gameRoom, GameResult.ONGOING))
                .thenReturn(Optional.of(gameMatch));
            
            // Mock existing move at (0,0)
            Move existingMove = new Move();
            existingMove.setXPosition(0);
            existingMove.setYPosition(0);
            existingMove.setPlayer(playerX);
            when(moveRepository.findByMatchOrderByMoveNumber(gameMatch))
                .thenReturn(Arrays.asList(existingMove));
            
            // When & Then
            CustomException exception = assertThrows(CustomException.class, 
                () -> caroGameService.makeMove(1L, request, 1L));
            
            assertEquals(StatusCode.INVALID_GAME_MOVE, exception.getStatusCode());
            verify(moveRepository, never()).save(any(Move.class));
        }
        
        @Test
        @DisplayName("Should reject move when not player's turn")
        void shouldRejectMoveWhenNotPlayersTurn() {
            // Given
            GameMoveRequest request = new GameMoveRequest();
            request.setXPosition(1);
            request.setYPosition(1);
            
            when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
            when(gameMatchRepository.findByRoomAndResult(gameRoom, GameResult.ONGOING))
                .thenReturn(Optional.of(gameMatch));
            when(moveRepository.countByMatch(gameMatch)).thenReturn(1); // O's turn
            
            // When & Then - PlayerX tries to move when it's O's turn
            CustomException exception = assertThrows(CustomException.class, 
                () -> caroGameService.makeMove(1L, request, 1L));
            
            assertEquals(StatusCode.NOT_PLAYER_TURN, exception.getStatusCode());
            verify(moveRepository, never()).save(any(Move.class));
        }
        
        @Test
        @DisplayName("Should reject move on invalid game state")
        void shouldRejectMoveOnInvalidGameState() {
            // Given
            gameRoom.setGameState(GameState.WAITING_FOR_READY);
            
            GameMoveRequest request = new GameMoveRequest();
            request.setXPosition(0);
            request.setYPosition(0);
            
            when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
            
            // When & Then
            CustomException exception = assertThrows(CustomException.class, 
                () -> caroGameService.makeMove(1L, request, 1L));
            
            assertEquals(StatusCode.GAME_NOT_ACTIVE, exception.getStatusCode());
        }
        
        @Test
        @DisplayName("Should reject null move request")
        void shouldRejectNullMoveRequest() {
            // When & Then
            CustomException exception = assertThrows(CustomException.class, 
                () -> caroGameService.makeMove(1L, null, 1L));
            
            assertEquals(StatusCode.INVALID_REQUEST, exception.getStatusCode());
        }
        
        @Test
        @DisplayName("Should reject move with null coordinates")
        void shouldRejectMoveWithNullCoordinates() {
            // Given
            GameMoveRequest request = new GameMoveRequest();
            request.setXPosition(null);
            request.setYPosition(0);
            
            // When & Then
            CustomException exception = assertThrows(CustomException.class, 
                () -> caroGameService.makeMove(1L, request, 1L));
            
            assertEquals(StatusCode.INVALID_REQUEST, exception.getStatusCode());
        }
    }
    
    @Nested
    @DisplayName("Win Detection Tests")
    class WinDetectionTests {
        
        @Test
        @DisplayName("Should detect horizontal win")
        void shouldDetectHorizontalWin() {
            // Given
            int[][] board = new int[15][15];
            // Place 5 X's horizontally
            for (int i = 0; i < 5; i++) {
                board[0][i] = CaroGameConstants.PLAYER_X_VALUE;
            }
            
            // When & Then
            assertTrue(caroGameService.checkWin(board, 0, 4, CaroGameConstants.PLAYER_X_VALUE));
            assertTrue(caroGameService.checkWin(board, 0, 2, CaroGameConstants.PLAYER_X_VALUE));
            assertFalse(caroGameService.checkWin(board, 1, 1, CaroGameConstants.PLAYER_X_VALUE));
        }
        
        @Test
        @DisplayName("Should detect vertical win")
        void shouldDetectVerticalWin() {
            // Given
            int[][] board = new int[15][15];
            // Place 5 O's vertically
            for (int i = 0; i < 5; i++) {
                board[i][0] = CaroGameConstants.PLAYER_O_VALUE;
            }
            
            // When & Then
            assertTrue(caroGameService.checkWin(board, 4, 0, CaroGameConstants.PLAYER_O_VALUE));
            assertTrue(caroGameService.checkWin(board, 2, 0, CaroGameConstants.PLAYER_O_VALUE));
            assertFalse(caroGameService.checkWin(board, 0, 1, CaroGameConstants.PLAYER_O_VALUE));
        }
        
        @Test
        @DisplayName("Should detect diagonal win (top-left to bottom-right)")
        void shouldDetectDiagonalWinTLBR() {
            // Given
            int[][] board = new int[15][15];
            // Place 5 X's diagonally
            for (int i = 0; i < 5; i++) {
                board[i][i] = CaroGameConstants.PLAYER_X_VALUE;
            }
            
            // When & Then
            assertTrue(caroGameService.checkWin(board, 2, 2, CaroGameConstants.PLAYER_X_VALUE));
            assertTrue(caroGameService.checkWin(board, 0, 0, CaroGameConstants.PLAYER_X_VALUE));
            assertTrue(caroGameService.checkWin(board, 4, 4, CaroGameConstants.PLAYER_X_VALUE));
        }
        
        @Test
        @DisplayName("Should detect diagonal win (top-right to bottom-left)")
        void shouldDetectDiagonalWinTRBL() {
            // Given
            int[][] board = new int[15][15];
            // Place 5 O's diagonally (top-right to bottom-left)
            for (int i = 0; i < 5; i++) {
                board[i][4-i] = CaroGameConstants.PLAYER_O_VALUE;
            }
            
            // When & Then
            assertTrue(caroGameService.checkWin(board, 2, 2, CaroGameConstants.PLAYER_O_VALUE));
            assertTrue(caroGameService.checkWin(board, 0, 4, CaroGameConstants.PLAYER_O_VALUE));
            assertTrue(caroGameService.checkWin(board, 4, 0, CaroGameConstants.PLAYER_O_VALUE));
        }
        
        @Test
        @DisplayName("Should not detect win with only 4 in a row")
        void shouldNotDetectWinWithFourInRow() {
            // Given
            int[][] board = new int[15][15];
            // Place only 4 X's horizontally
            for (int i = 0; i < 4; i++) {
                board[0][i] = CaroGameConstants.PLAYER_X_VALUE;
            }
            
            // When & Then
            assertFalse(caroGameService.checkWin(board, 0, 3, CaroGameConstants.PLAYER_X_VALUE));
        }
        
        @Test
        @DisplayName("Should detect draw when board is full")
        void shouldDetectDrawWhenBoardFull() {
            // Given
            int[][] board = new int[15][15];
            // Fill entire board alternating X and O
            for (int i = 0; i < 15; i++) {
                for (int j = 0; j < 15; j++) {
                    board[i][j] = (i + j) % 2 == 0 ? 
                        CaroGameConstants.PLAYER_X_VALUE : CaroGameConstants.PLAYER_O_VALUE;
                }
            }
            
            // When & Then
            assertTrue(caroGameService.checkDraw(board));
        }
        
        @Test
        @DisplayName("Should not detect draw when board has empty cells")
        void shouldNotDetectDrawWithEmptyCells() {
            // Given
            int[][] board = new int[15][15];
            // Leave some cells empty
            board[0][0] = CaroGameConstants.PLAYER_X_VALUE;
            board[0][1] = CaroGameConstants.PLAYER_O_VALUE;
            // Rest are empty (0)
            
            // When & Then
            assertFalse(caroGameService.checkDraw(board));
        }
    }
    
    @Nested
    @DisplayName("Move Validation Tests")
    class MoveValidationTests {
        
        @Test
        @DisplayName("Should validate move within bounds")
        void shouldValidateMoveWithinBounds() {
            // Given
            int[][] board = new int[15][15];
            
            // When & Then
            assertTrue(caroGameService.isValidMove(board, 0, 0));
            assertTrue(caroGameService.isValidMove(board, 7, 7));
            assertTrue(caroGameService.isValidMove(board, 14, 14));
        }
        
        @Test
        @DisplayName("Should reject move outside bounds")
        void shouldRejectMoveOutsideBounds() {
            // Given
            int[][] board = new int[15][15];
            
            // When & Then
            assertFalse(caroGameService.isValidMove(board, -1, 0));
            assertFalse(caroGameService.isValidMove(board, 0, -1));
            assertFalse(caroGameService.isValidMove(board, 15, 0));
            assertFalse(caroGameService.isValidMove(board, 0, 15));
            assertFalse(caroGameService.isValidMove(board, 20, 20));
        }
        
        @Test
        @DisplayName("Should reject move on occupied cell")
        void shouldRejectMoveOnOccupiedCell() {
            // Given
            int[][] board = new int[15][15];
            board[5][5] = CaroGameConstants.PLAYER_X_VALUE;
            board[7][7] = CaroGameConstants.PLAYER_O_VALUE;
            
            // When & Then
            assertFalse(caroGameService.isValidMove(board, 5, 5));
            assertFalse(caroGameService.isValidMove(board, 7, 7));
            assertTrue(caroGameService.isValidMove(board, 6, 6)); // Empty cell
        }
    }
    
    @Nested
    @DisplayName("Game Flow Tests")
    class GameFlowTests {
        
        @Test
        @DisplayName("Should handle game completion with winner")
        void shouldHandleGameCompletionWithWinner() {
            // Given
            GameMoveRequest request = new GameMoveRequest();
            request.setXPosition(0);
            request.setYPosition(4);
            
            when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
            when(gameMatchRepository.findByRoomAndResult(gameRoom, GameResult.ONGOING))
                .thenReturn(Optional.of(gameMatch));
            lenient().when(moveRepository.countByMatch(gameMatch)).thenReturn(0); // X's turn
            lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(playerX));
            
            // Setup existing moves to create winning condition
            List<Move> existingMoves = Arrays.asList(
                createMove(0, 0, playerX, 1),
                createMove(1, 0, playerO, 2),
                createMove(0, 1, playerX, 3),
                createMove(1, 1, playerO, 4),
                createMove(0, 2, playerX, 5),
                createMove(1, 2, playerO, 6),
                createMove(0, 3, playerX, 7)
            );
            lenient().when(moveRepository.findByMatchOrderByMoveNumber(gameMatch))
                .thenReturn(existingMoves);
            
            Move savedMove = createMove(0, 4, playerX, 8);
            lenient().when(moveRepository.save(any(Move.class))).thenReturn(savedMove);
            
            List<RoomPlayer> roomPlayers = Arrays.asList(roomPlayerX, roomPlayerO);
            lenient().when(roomPlayerRepository.findByRoomId(1L)).thenReturn(roomPlayers);
            
            // When
            GameMoveResponse response = caroGameService.makeMove(1L, request, 1L);
            
            // Then
            assertNotNull(response);
            assertEquals(GameState.FINISHED, response.getGameState());
            assertEquals(1L, response.getWinnerId());
            assertTrue(response.getMessage().contains("won"));
            
            verify(gameMatchRepository).save(gameMatch);
            verify(gameRoomRepository).save(gameRoom);
            verify(gameHistoryRepository).save(any(GameHistory.class));
        }
        
        @Test
        @DisplayName("Should create new match when room is IN_PROGRESS but no active match")
        void shouldCreateNewMatchWhenNeeded() {
            // Given
            GameMoveRequest request = new GameMoveRequest();
            request.setXPosition(0);
            request.setYPosition(0);
            
            when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
            when(gameMatchRepository.findByRoomAndResult(gameRoom, GameResult.ONGOING))
                .thenReturn(Optional.empty()); // No existing match
            when(gameMatchRepository.save(any(GameMatch.class))).thenReturn(gameMatch);
            when(moveRepository.countByMatch(gameMatch)).thenReturn(0);
            when(userRepository.findById(1L)).thenReturn(Optional.of(playerX));
            
            Move savedMove = createMove(0, 0, playerX, 1);
            when(moveRepository.save(any(Move.class))).thenReturn(savedMove);
            
            // When
            GameMoveResponse response = caroGameService.makeMove(1L, request, 1L);
            
            // Then
            assertNotNull(response);
            verify(gameMatchRepository, atLeast(1)).save(any(GameMatch.class)); // May be called multiple times due to player assignment fixes
            verify(moveRepository).save(any(Move.class));
        }
        
        private Move createMove(int x, int y, User player, int moveNumber) {
            Move move = new Move();
            move.setXPosition(x);
            move.setYPosition(y);
            move.setPlayer(player);
            move.setMoveNumber(moveNumber);
            move.setMatch(gameMatch);
            return move;
        }
    }
    
    @Nested
    @DisplayName("Board State Tests")
    class BoardStateTests {
        
        @Test
        @DisplayName("Should correctly build board from moves")
        void shouldCorrectlyBuildBoardFromMoves() {
            // Given
            List<Move> moves = Arrays.asList(
                createMoveWithId(0, 0, playerX, 1),
                createMoveWithId(1, 1, playerO, 2),
                createMoveWithId(0, 1, playerX, 3),
                createMoveWithId(1, 0, playerO, 4)
            );
            
            when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
            when(gameMatchRepository.findByRoomAndResult(gameRoom, GameResult.ONGOING))
                .thenReturn(Optional.of(gameMatch));
            when(moveRepository.findByMatchOrderByMoveNumber(gameMatch)).thenReturn(moves);
            
            // When
            int[][] board = caroGameService.getCurrentBoard(1L);
            
            // Then
            assertEquals(CaroGameConstants.PLAYER_X_VALUE, board[0][0]);
            assertEquals(CaroGameConstants.PLAYER_O_VALUE, board[1][1]);
            assertEquals(CaroGameConstants.PLAYER_X_VALUE, board[0][1]);
            assertEquals(CaroGameConstants.PLAYER_O_VALUE, board[1][0]);
            assertEquals(CaroGameConstants.EMPTY_CELL, board[2][2]);
        }
        
        @Test
        @DisplayName("Should return player symbol correctly")
        void shouldReturnPlayerSymbolCorrectly() {
            // Given
            when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(gameRoom));
            when(gameMatchRepository.findByRoomAndResult(gameRoom, GameResult.ONGOING))
                .thenReturn(Optional.of(gameMatch));
            
            // When & Then
            assertEquals("X", caroGameService.getPlayerSymbol(1L, 1L)); // PlayerX
            assertEquals("O", caroGameService.getPlayerSymbol(1L, 2L)); // PlayerO
        }
        
        private Move createMoveWithId(int x, int y, User player, int moveNumber) {
            Move move = new Move();
            move.setId((long) moveNumber);
            move.setXPosition(x);
            move.setYPosition(y);
            move.setPlayer(player);
            move.setMoveNumber(moveNumber);
            move.setMatch(gameMatch);
            return move;
        }
    }
}
