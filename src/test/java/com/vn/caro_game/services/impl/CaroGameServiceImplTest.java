package com.vn.caro_game.services.impl;

import com.vn.caro_game.constants.CaroGameConstants;
import com.vn.caro_game.dtos.request.GameMoveRequest;
import com.vn.caro_game.dtos.response.GameMoveResponse;
import com.vn.caro_game.entities.GameMatch;
import com.vn.caro_game.entities.GameRoom;
import com.vn.caro_game.entities.Move;
import com.vn.caro_game.entities.RoomPlayer;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.GameResult;
import com.vn.caro_game.enums.GameState;
import com.vn.caro_game.enums.PlayerReadyState;
import com.vn.caro_game.enums.StatusCode;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.repositories.GameMatchRepository;
import com.vn.caro_game.repositories.GameRoomRepository;
import com.vn.caro_game.repositories.MoveRepository;
import com.vn.caro_game.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CaroGameServiceImpl.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class CaroGameServiceImplTest {

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

    @InjectMocks
    private CaroGameServiceImpl caroGameService;

    private GameRoom testRoom;
    private GameMatch testMatch;
    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        // Setup test users
        testUser1 = new User();
        testUser1.setId(1L);
        testUser1.setUsername("player1");

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setUsername("player2");

        // Setup test room
        testRoom = new GameRoom();
        testRoom.setId(1L);
        testRoom.setGameState(GameState.IN_PROGRESS);

        // Setup RoomPlayers
        RoomPlayer roomPlayer1 = new RoomPlayer();
        roomPlayer1.setId(new RoomPlayer.RoomPlayerId(1L, 1L));
        roomPlayer1.setRoom(testRoom);
        roomPlayer1.setUser(testUser1);
        roomPlayer1.setIsHost(true);
        roomPlayer1.setReadyState(PlayerReadyState.READY);

        RoomPlayer roomPlayer2 = new RoomPlayer();
        roomPlayer2.setId(new RoomPlayer.RoomPlayerId(1L, 2L));
        roomPlayer2.setRoom(testRoom);
        roomPlayer2.setUser(testUser2);
        roomPlayer2.setIsHost(false);
        roomPlayer2.setReadyState(PlayerReadyState.READY);

        // Add players to room
        Set<RoomPlayer> roomPlayers = new HashSet<>();
        roomPlayers.add(roomPlayer1);
        roomPlayers.add(roomPlayer2);
        testRoom.setRoomPlayers(roomPlayers);

        // Setup test match
        testMatch = new GameMatch();
        testMatch.setId(1L);
        testMatch.setRoom(testRoom);
        testMatch.setPlayerX(testUser1);
        testMatch.setPlayerO(testUser2);
        testMatch.setResult(GameResult.ONGOING);
    }

    @Test
    void testInitializeBoard() {
        // When
        int[][] board = caroGameService.initializeBoard();

        // Then
        assertNotNull(board);
        assertEquals(CaroGameConstants.BOARD_SIZE, board.length);
        assertEquals(CaroGameConstants.BOARD_SIZE, board[0].length);
        
        // Check all cells are empty
        for (int i = 0; i < CaroGameConstants.BOARD_SIZE; i++) {
            for (int j = 0; j < CaroGameConstants.BOARD_SIZE; j++) {
                assertEquals(CaroGameConstants.EMPTY_CELL, board[i][j]);
            }
        }
    }

    @Test
    void testIsValidMove_ValidMove() {
        // Given
        int[][] board = caroGameService.initializeBoard();
        
        // When & Then
        assertTrue(caroGameService.isValidMove(board, 7, 7));
        assertTrue(caroGameService.isValidMove(board, 0, 0));
        assertTrue(caroGameService.isValidMove(board, 14, 14));
    }

    @Test
    void testIsValidMove_InvalidMove_OutOfBounds() {
        // Given
        int[][] board = caroGameService.initializeBoard();
        
        // When & Then
        assertFalse(caroGameService.isValidMove(board, -1, 7));
        assertFalse(caroGameService.isValidMove(board, 7, -1));
        assertFalse(caroGameService.isValidMove(board, 15, 7));
        assertFalse(caroGameService.isValidMove(board, 7, 15));
    }

    @Test
    void testIsValidMove_InvalidMove_CellOccupied() {
        // Given
        int[][] board = caroGameService.initializeBoard();
        board[7][7] = CaroGameConstants.PLAYER_X_VALUE;
        
        // When & Then
        assertFalse(caroGameService.isValidMove(board, 7, 7));
    }

    @Test
    void testCheckWin_HorizontalWin() {
        // Given
        int[][] board = caroGameService.initializeBoard();
        int playerValue = CaroGameConstants.PLAYER_X_VALUE;
        
        // Place 4 pieces horizontally
        for (int i = 0; i < 4; i++) {
            board[7][i] = playerValue;
        }
        
        // When - place the 5th piece
        board[7][4] = playerValue;
        
        // Then
        assertTrue(caroGameService.checkWin(board, 7, 4, playerValue));
    }

    @Test
    void testCheckWin_VerticalWin() {
        // Given
        int[][] board = caroGameService.initializeBoard();
        int playerValue = CaroGameConstants.PLAYER_O_VALUE;
        
        // Place 4 pieces vertically
        for (int i = 0; i < 4; i++) {
            board[i][7] = playerValue;
        }
        
        // When - place the 5th piece
        board[4][7] = playerValue;
        
        // Then
        assertTrue(caroGameService.checkWin(board, 4, 7, playerValue));
    }

    @Test
    void testCheckWin_DiagonalWin() {
        // Given
        int[][] board = caroGameService.initializeBoard();
        int playerValue = CaroGameConstants.PLAYER_X_VALUE;
        
        // Place 4 pieces diagonally
        for (int i = 0; i < 4; i++) {
            board[i][i] = playerValue;
        }
        
        // When - place the 5th piece
        board[4][4] = playerValue;
        
        // Then
        assertTrue(caroGameService.checkWin(board, 4, 4, playerValue));
    }

    @Test
    void testCheckWin_NoWin() {
        // Given
        int[][] board = caroGameService.initializeBoard();
        int playerValue = CaroGameConstants.PLAYER_X_VALUE;
        
        // Place only 4 pieces horizontally
        for (int i = 0; i < 4; i++) {
            board[7][i] = playerValue;
        }
        
        // When & Then
        assertFalse(caroGameService.checkWin(board, 7, 3, playerValue));
    }

    @Test
    void testCheckDraw_BoardFull() {
        // Given
        int[][] board = caroGameService.initializeBoard();
        
        // Fill the entire board alternately
        for (int i = 0; i < CaroGameConstants.BOARD_SIZE; i++) {
            for (int j = 0; j < CaroGameConstants.BOARD_SIZE; j++) {
                board[i][j] = (i + j) % 2 == 0 ? 
                    CaroGameConstants.PLAYER_X_VALUE : CaroGameConstants.PLAYER_O_VALUE;
            }
        }
        
        // When & Then
        assertTrue(caroGameService.checkDraw(board));
    }

    @Test
    void testCheckDraw_BoardNotFull() {
        // Given
        int[][] board = caroGameService.initializeBoard();
        board[0][0] = CaroGameConstants.PLAYER_X_VALUE;
        
        // When & Then
        assertFalse(caroGameService.checkDraw(board));
    }

    @Test
    void testGetPlayerValue() {
        // Given
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(gameMatchRepository.findByRoomAndResult(testRoom, GameResult.ONGOING))
                .thenReturn(Optional.of(testMatch));
        
        // When & Then
        assertEquals(CaroGameConstants.PLAYER_X_VALUE, 
                caroGameService.getPlayerValue(1L, 1L));
        assertEquals(CaroGameConstants.PLAYER_O_VALUE, 
                caroGameService.getPlayerValue(1L, 2L));
        assertEquals(CaroGameConstants.EMPTY_CELL, 
                caroGameService.getPlayerValue(1L, 3L));
    }

    @Test
    void testGetPlayerSymbol() {
        // Given
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(gameMatchRepository.findByRoomAndResult(testRoom, GameResult.ONGOING))
                .thenReturn(Optional.of(testMatch));
        
        // When & Then
        assertEquals(CaroGameConstants.PLAYER_X_SYMBOL, 
                caroGameService.getPlayerSymbol(1L, 1L));
        assertEquals(CaroGameConstants.PLAYER_O_SYMBOL, 
                caroGameService.getPlayerSymbol(1L, 2L));
    }

    @Test
    void testGetCurrentBoard() {
        // Given
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(gameMatchRepository.findByRoomAndResult(testRoom, GameResult.ONGOING))
                .thenReturn(Optional.of(testMatch));
        
        Move move1 = new Move();
        move1.setXPosition(7);
        move1.setYPosition(7);
        move1.setPlayer(testUser1);
        move1.setMoveNumber(1);
        
        when(moveRepository.findByMatchOrderByMoveNumber(testMatch))
                .thenReturn(Arrays.asList(move1));
        
        // When
        int[][] board = caroGameService.getCurrentBoard(1L);
        
        // Then
        assertNotNull(board);
        assertEquals(CaroGameConstants.PLAYER_X_VALUE, board[7][7]);
        assertEquals(CaroGameConstants.EMPTY_CELL, board[0][0]);
    }

    @Test
    void testMakeMove_ValidMove() {
        // Given
        GameMoveRequest request = new GameMoveRequest();
        request.setXPosition(7);
        request.setYPosition(7);
        
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(gameMatchRepository.findByRoomAndResult(testRoom, GameResult.ONGOING))
                .thenReturn(Optional.of(testMatch));
        when(moveRepository.findByMatchOrderByMoveNumber(testMatch))
                .thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser1));
        when(moveRepository.countByMatch(testMatch)).thenReturn(0);
        
        Move savedMove = new Move();
        savedMove.setId(1L);
        savedMove.setXPosition(7);
        savedMove.setYPosition(7);
        savedMove.setPlayer(testUser1);
        savedMove.setMoveNumber(1);
        savedMove.setMatch(testMatch);
        
        when(moveRepository.save(any(Move.class))).thenReturn(savedMove);
        
        // When
        GameMoveResponse response = caroGameService.makeMove(1L, request, 1L);
        
        // Then
        assertNotNull(response);
        assertEquals(1L, response.getRoomId());
        assertEquals(1L, response.getMatchId());
        assertEquals(7, response.getXPosition());
        assertEquals(7, response.getYPosition());
        assertEquals(1L, response.getPlayerId());
        assertEquals(CaroGameConstants.PLAYER_X_SYMBOL, response.getPlayerSymbol());
        assertEquals(1, response.getMoveNumber());
        assertTrue(response.getIsValidMove());
        
        verify(messagingTemplate).convertAndSend(anyString(), any(GameMoveResponse.class));
    }

    @Test
    void testMakeMove_InvalidMove_RoomNotFound() {
        // Given
        GameMoveRequest request = new GameMoveRequest();
        request.setXPosition(7);
        request.setYPosition(7);
        
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> 
                caroGameService.makeMove(1L, request, 1L));
        
        assertEquals(StatusCode.ROOM_NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testMakeMove_InvalidMove_GameNotActive() {
        // Given
        testRoom.setGameState(GameState.FINISHED);
        GameMoveRequest request = new GameMoveRequest();
        request.setXPosition(7);
        request.setYPosition(7);
        
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        
        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> 
                caroGameService.makeMove(1L, request, 1L));
        
        assertEquals(StatusCode.GAME_NOT_ACTIVE, exception.getStatusCode());
    }

    @Test
    void testMakeMove_InvalidMove_CellOccupied() {
        // Given
        GameMoveRequest request = new GameMoveRequest();
        request.setXPosition(7);
        request.setYPosition(7);
        
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(gameMatchRepository.findByRoomAndResult(testRoom, GameResult.ONGOING))
                .thenReturn(Optional.of(testMatch));
        
        Move existingMove = new Move();
        existingMove.setXPosition(7);
        existingMove.setYPosition(7);
        existingMove.setPlayer(testUser1);
        
        when(moveRepository.findByMatchOrderByMoveNumber(testMatch))
                .thenReturn(Arrays.asList(existingMove));
        
        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> 
                caroGameService.makeMove(1L, request, 1L));
        
        assertEquals(StatusCode.INVALID_GAME_MOVE, exception.getStatusCode());
    }
}
