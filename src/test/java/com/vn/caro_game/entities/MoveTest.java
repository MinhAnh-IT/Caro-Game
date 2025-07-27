package com.vn.caro_game.entities;

import com.vn.caro_game.enums.GameResult;
import com.vn.caro_game.enums.RoomStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Move Entity Tests")
class MoveTest {

    private User playerX;
    private User playerO;
    private GameRoom testRoom;
    private GameMatch testMatch;
    private Move move;

    @BeforeEach
    void setUp() {
        playerX = new User();
        playerX.setId(1L);
        playerX.setUsername("playerX");
        playerX.setEmail("playerx@example.com");
        playerX.setPassword("hashedPassword");

        playerO = new User();
        playerO.setId(2L);
        playerO.setUsername("playerO");
        playerO.setEmail("playero@example.com");
        playerO.setPassword("hashedPassword");

        testRoom = new GameRoom();
        testRoom.setId(1L);
        testRoom.setName("Test Room");
        testRoom.setCreatedBy(playerX);
        testRoom.setStatus(RoomStatus.PLAYING);
        testRoom.setIsPrivate(false);

        testMatch = new GameMatch();
        testMatch.setId(1L);
        testMatch.setRoom(testRoom);
        testMatch.setPlayerX(playerX);
        testMatch.setPlayerO(playerO);
        testMatch.setResult(GameResult.ONGOING);

        move = new Move();
        move.setId(1L);
        move.setMatch(testMatch);
        move.setPlayer(playerX);
        move.setXPosition(5);
        move.setYPosition(7);
        move.setMoveNumber(1);
        move.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create Move with all required fields")
    void shouldCreateMoveWithAllRequiredFields() {
        assertThat(move).isNotNull();
        assertThat(move.getId()).isEqualTo(1L);
        assertThat(move.getMatch()).isEqualTo(testMatch);
        assertThat(move.getPlayer()).isEqualTo(playerX);
        assertThat(move.getXPosition()).isEqualTo(5);
        assertThat(move.getYPosition()).isEqualTo(7);
        assertThat(move.getMoveNumber()).isEqualTo(1);
        assertThat(move.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle coordinate positions correctly")
    void shouldHandleCoordinatePositionsCorrectly() {
        // Test valid coordinates
        move.setXPosition(0);
        move.setYPosition(0);
        assertThat(move.getXPosition()).isEqualTo(0);
        assertThat(move.getYPosition()).isEqualTo(0);

        // Test maximum coordinates (15x15 board)
        move.setXPosition(14);
        move.setYPosition(14);
        assertThat(move.getXPosition()).isEqualTo(14);
        assertThat(move.getYPosition()).isEqualTo(14);

        // Test middle coordinates
        move.setXPosition(7);
        move.setYPosition(7);
        assertThat(move.getXPosition()).isEqualTo(7);
        assertThat(move.getYPosition()).isEqualTo(7);
    }

    @Test
    @DisplayName("Should handle move number sequencing")
    void shouldHandleMoveNumberSequencing() {
        move.setMoveNumber(1);
        assertThat(move.getMoveNumber()).isEqualTo(1);

        move.setMoveNumber(100);
        assertThat(move.getMoveNumber()).isEqualTo(100);

        move.setMoveNumber(0);
        assertThat(move.getMoveNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should maintain relationship with GameMatch")
    void shouldMaintainRelationshipWithGameMatch() {
        assertThat(move.getMatch()).isEqualTo(testMatch);
        
        GameMatch newMatch = new GameMatch();
        newMatch.setId(2L);
        newMatch.setRoom(testRoom);
        newMatch.setPlayerX(playerX);
        newMatch.setPlayerO(playerO);
        
        move.setMatch(newMatch);
        assertThat(move.getMatch()).isEqualTo(newMatch);
        assertThat(move.getMatch().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should maintain relationship with Player")
    void shouldMaintainRelationshipWithPlayer() {
        assertThat(move.getPlayer()).isEqualTo(playerX);
        
        move.setPlayer(playerO);
        assertThat(move.getPlayer()).isEqualTo(playerO);
        assertThat(move.getPlayer().getUsername()).isEqualTo("playerO");
    }

    @Test
    @DisplayName("Should handle timestamp creation")
    void shouldHandleTimestampCreation() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        LocalDateTime timestamp = LocalDateTime.now();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        move.setCreatedAt(timestamp);
        
        assertThat(move.getCreatedAt()).isAfter(before);
        assertThat(move.getCreatedAt()).isBefore(after);
        assertThat(move.getCreatedAt()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("Should handle null values appropriately")
    void shouldHandleNullValuesAppropriately() {
        Move newMove = new Move();
        
        assertThat(newMove.getId()).isNull();
        assertThat(newMove.getMatch()).isNull();
        assertThat(newMove.getPlayer()).isNull();
        assertThat(newMove.getXPosition()).isNull();
        assertThat(newMove.getYPosition()).isNull();
        assertThat(newMove.getMoveNumber()).isNull();
        assertThat(newMove.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Should validate coordinate bounds")
    void shouldValidateCoordinateBounds() {
        // Test edge cases for coordinate validation
        move.setXPosition(-1);
        move.setYPosition(-1);
        assertThat(move.getXPosition()).isEqualTo(-1);
        assertThat(move.getYPosition()).isEqualTo(-1);

        move.setXPosition(15);
        move.setYPosition(15);
        assertThat(move.getXPosition()).isEqualTo(15);
        assertThat(move.getYPosition()).isEqualTo(15);
    }

    @Test
    @DisplayName("Should support move sequence ordering")
    void shouldSupportMoveSequenceOrdering() {
        Move move1 = new Move();
        move1.setMoveNumber(1);
        move1.setCreatedAt(LocalDateTime.now().minusMinutes(2));

        Move move2 = new Move();
        move2.setMoveNumber(2);
        move2.setCreatedAt(LocalDateTime.now().minusMinutes(1));

        Move move3 = new Move();
        move3.setMoveNumber(3);
        move3.setCreatedAt(LocalDateTime.now());

        assertThat(move1.getMoveNumber()).isLessThan(move2.getMoveNumber());
        assertThat(move2.getMoveNumber()).isLessThan(move3.getMoveNumber());
        assertThat(move1.getCreatedAt()).isBefore(move2.getCreatedAt());
        assertThat(move2.getCreatedAt()).isBefore(move3.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle different players in sequence")
    void shouldHandleDifferentPlayersInSequence() {
        // First move by player X
        Move move1 = new Move();
        move1.setPlayer(playerX);
        move1.setMoveNumber(1);
        move1.setXPosition(7);
        move1.setYPosition(7);

        // Second move by player O
        Move move2 = new Move();
        move2.setPlayer(playerO);
        move2.setMoveNumber(2);
        move2.setXPosition(7);
        move2.setYPosition(8);

        assertThat(move1.getPlayer()).isEqualTo(playerX);
        assertThat(move2.getPlayer()).isEqualTo(playerO);
        assertThat(move1.getPlayer()).isNotEqualTo(move2.getPlayer());
    }

    @Test
    @DisplayName("Should handle position updates")
    void shouldHandlePositionUpdates() {
        assertThat(move.getXPosition()).isEqualTo(5);
        assertThat(move.getYPosition()).isEqualTo(7);

        move.setXPosition(10);
        move.setYPosition(12);

        assertThat(move.getXPosition()).isEqualTo(10);
        assertThat(move.getYPosition()).isEqualTo(12);
    }

    @Test
    @DisplayName("Should maintain referential integrity with match")
    void shouldMaintainReferentialIntegrityWithMatch() {
        assertThat(move.getMatch()).isNotNull();
        assertThat(move.getMatch().getRoom()).isEqualTo(testRoom);
        assertThat(move.getMatch().getPlayerX()).isEqualTo(playerX);
        assertThat(move.getMatch().getPlayerO()).isEqualTo(playerO);
    }

    @Test
    @DisplayName("Should handle duplicate position detection")
    void shouldHandleDuplicatePositionDetection() {
        Move move1 = new Move();
        move1.setXPosition(5);
        move1.setYPosition(5);

        Move move2 = new Move();
        move2.setXPosition(5);
        move2.setYPosition(5);

        assertThat(move1.getXPosition()).isEqualTo(move2.getXPosition());
        assertThat(move1.getYPosition()).isEqualTo(move2.getYPosition());
    }
}
