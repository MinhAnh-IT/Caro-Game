package com.vn.caro_game.repositories;
import com.vn.caro_game.enums.GameResult;import com.vn.caro_game.enums.RoomStatus;
import com.vn.caro_game.entities.GameMatch;
import com.vn.caro_game.entities.GameRoom;
import com.vn.caro_game.entities.Move;
import com.vn.caro_game.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("MoveRepository Tests")
class MoveRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MoveRepository moveRepository;

    private User playerX;
    private User playerO;
    private GameRoom testRoom;
    private GameMatch testMatch;
    private Move testMove;

    @BeforeEach
    void setUp() {
        playerX = new User();
        playerX.setUsername("playerX");
        playerX.setEmail("playerX@example.com");
        playerX.setPassword("hashedPassword");
        playerX = entityManager.persistAndFlush(playerX);

        playerO = new User();
        playerO.setUsername("playerO");
        playerO.setEmail("playerO@example.com");
        playerO.setPassword("hashedPassword");
        playerO = entityManager.persistAndFlush(playerO);

        testRoom = new GameRoom();
        testRoom.setName("Test Room");
        testRoom.setCreatedBy(playerX);
        testRoom.setStatus(RoomStatus.PLAYING);
        testRoom.setIsPrivate(false);
        testRoom = entityManager.persistAndFlush(testRoom);

        testMatch = new GameMatch();
        testMatch.setRoom(testRoom);
        testMatch.setPlayerX(playerX);
        testMatch.setPlayerO(playerO);
        testMatch.setResult(GameResult.ONGOING);
        testMatch = entityManager.persistAndFlush(testMatch);

        testMove = new Move();
        testMove.setMatch(testMatch);
        testMove.setPlayer(playerX);
        testMove.setXPosition(5);
        testMove.setYPosition(5);
        testMove.setMoveNumber(1);
    }

    @Test
    @DisplayName("Should save and retrieve move")
    void shouldSaveAndRetrieveMove() {
        // When
        Move savedMove = moveRepository.save(testMove);

        // Then
        assertThat(savedMove.getId()).isNotNull();
        assertThat(savedMove.getMatch().getId()).isEqualTo(testMatch.getId());
        assertThat(savedMove.getPlayer().getId()).isEqualTo(playerX.getId());
        assertThat(savedMove.getXPosition()).isEqualTo(5);
        assertThat(savedMove.getYPosition()).isEqualTo(5);
        assertThat(savedMove.getMoveNumber()).isEqualTo(1);
        assertThat(savedMove.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find moves by match ID ordered by move number")
    void shouldFindMovesByMatchIdOrderedByMoveNumber() {
        // Given
        entityManager.persistAndFlush(testMove);
        
        Move secondMove = new Move();
        secondMove.setMatch(testMatch);
        secondMove.setPlayer(playerO);
        secondMove.setXPosition(6);
        secondMove.setYPosition(5);
        secondMove.setMoveNumber(2);
        entityManager.persistAndFlush(secondMove);
        
        Move thirdMove = new Move();
        thirdMove.setMatch(testMatch);
        thirdMove.setPlayer(playerX);
        thirdMove.setXPosition(7);
        thirdMove.setYPosition(5);
        thirdMove.setMoveNumber(3);
        entityManager.persistAndFlush(thirdMove);

        // When
        List<Move> moves = moveRepository.findByMatchIdOrderByMoveNumberAsc(testMatch.getId());

        // Then
        assertThat(moves).hasSize(3);
        assertThat(moves.get(0).getMoveNumber()).isEqualTo(1);
        assertThat(moves.get(1).getMoveNumber()).isEqualTo(2);
        assertThat(moves.get(2).getMoveNumber()).isEqualTo(3);
        
        assertThat(moves.get(0).getPlayer().getId()).isEqualTo(playerX.getId());
        assertThat(moves.get(1).getPlayer().getId()).isEqualTo(playerO.getId());
        assertThat(moves.get(2).getPlayer().getId()).isEqualTo(playerX.getId());
    }

    @Test
    @DisplayName("Should find moves by match ID and player ID")
    void shouldFindMovesByMatchIdAndPlayerId() {
        // Given
        entityManager.persistAndFlush(testMove);
        
        Move playerOMove = new Move();
        playerOMove.setMatch(testMatch);
        playerOMove.setPlayer(playerO);
        playerOMove.setXPosition(6);
        playerOMove.setYPosition(5);
        playerOMove.setMoveNumber(2);
        entityManager.persistAndFlush(playerOMove);
        
        Move anotherPlayerXMove = new Move();
        anotherPlayerXMove.setMatch(testMatch);
        anotherPlayerXMove.setPlayer(playerX);
        anotherPlayerXMove.setXPosition(7);
        anotherPlayerXMove.setYPosition(5);
        anotherPlayerXMove.setMoveNumber(3);
        entityManager.persistAndFlush(anotherPlayerXMove);

        // When
        List<Move> playerXMoves = moveRepository.findByMatchIdAndPlayerIdOrderByMoveNumber(
            testMatch.getId(), playerX.getId());
        List<Move> playerOMoves = moveRepository.findByMatchIdAndPlayerIdOrderByMoveNumber(
            testMatch.getId(), playerO.getId());

        // Then
        assertThat(playerXMoves).hasSize(2);
        assertThat(playerXMoves.get(0).getMoveNumber()).isEqualTo(1);
        assertThat(playerXMoves.get(1).getMoveNumber()).isEqualTo(3);
        
        assertThat(playerOMoves).hasSize(1);
        assertThat(playerOMoves.get(0).getMoveNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find last move by match ID")
    void shouldFindLastMoveByMatchId() {
        // Given
        entityManager.persistAndFlush(testMove);
        
        Move secondMove = new Move();
        secondMove.setMatch(testMatch);
        secondMove.setPlayer(playerO);
        secondMove.setXPosition(6);
        secondMove.setYPosition(5);
        secondMove.setMoveNumber(2);
        entityManager.persistAndFlush(secondMove);
        
        Move lastMove = new Move();
        lastMove.setMatch(testMatch);
        lastMove.setPlayer(playerX);
        lastMove.setXPosition(7);
        lastMove.setYPosition(5);
        lastMove.setMoveNumber(3);
        entityManager.persistAndFlush(lastMove);

        // When
        Move foundLastMove = moveRepository.findLastMoveByMatchId(testMatch.getId());

        // Then
        assertThat(foundLastMove).isNotNull();
        assertThat(foundLastMove.getMoveNumber()).isEqualTo(3);
        assertThat(foundLastMove.getPlayer().getId()).isEqualTo(playerX.getId());
        assertThat(foundLastMove.getXPosition()).isEqualTo(7);
        assertThat(foundLastMove.getYPosition()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should return null when no moves exist for match")
    void shouldReturnNullWhenNoMovesExistForMatch() {
        // When
        Move lastMove = moveRepository.findLastMoveByMatchId(testMatch.getId());

        // Then
        assertThat(lastMove).isNull();
    }

    @Test
    @DisplayName("Should count moves by match ID")
    void shouldCountMovesByMatchId() {
        // Given
        entityManager.persistAndFlush(testMove);
        
        Move secondMove = new Move();
        secondMove.setMatch(testMatch);
        secondMove.setPlayer(playerO);
        secondMove.setXPosition(6);
        secondMove.setYPosition(5);
        secondMove.setMoveNumber(2);
        entityManager.persistAndFlush(secondMove);

        // When
        long moveCount = moveRepository.countMovesByMatchId(testMatch.getId());

        // Then
        assertThat(moveCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should check if move exists at position")
    void shouldCheckIfMoveExistsAtPosition() {
        // Given
        entityManager.persistAndFlush(testMove);

        // When & Then
        assertThat(moveRepository.existsByMatchIdAndXPositionAndYPosition(
            testMatch.getId(), 5, 5)).isTrue();
        assertThat(moveRepository.existsByMatchIdAndXPositionAndYPosition(
            testMatch.getId(), 6, 6)).isFalse();
    }

    @Test
    @DisplayName("Should return empty list when match has no moves")
    void shouldReturnEmptyListWhenMatchHasNoMoves() {
        // When
        List<Move> moves = moveRepository.findByMatchIdOrderByMoveNumberAsc(testMatch.getId());

        // Then
        assertThat(moves).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when player has no moves in match")
    void shouldReturnEmptyListWhenPlayerHasNoMovesInMatch() {
        // Given
        entityManager.persistAndFlush(testMove); // Only playerX move

        // When
        List<Move> playerOMoves = moveRepository.findByMatchIdAndPlayerIdOrderByMoveNumber(
            testMatch.getId(), playerO.getId());

        // Then
        assertThat(playerOMoves).isEmpty();
    }

    @Test
    @DisplayName("Should return zero count when match has no moves")
    void shouldReturnZeroCountWhenMatchHasNoMoves() {
        // When
        long moveCount = moveRepository.countMovesByMatchId(testMatch.getId());

        // Then
        assertThat(moveCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle different match IDs correctly")
    void shouldHandleDifferentMatchIdsCorrectly() {
        // Given
        GameMatch anotherMatch = new GameMatch();
        anotherMatch.setRoom(testRoom);
        anotherMatch.setPlayerX(playerO);
        anotherMatch.setPlayerO(playerX);
        anotherMatch.setResult(GameResult.ONGOING);
        anotherMatch = entityManager.persistAndFlush(anotherMatch);
        
        entityManager.persistAndFlush(testMove); // Move for testMatch
        
        Move anotherMove = new Move();
        anotherMove.setMatch(anotherMatch);
        anotherMove.setPlayer(playerO);
        anotherMove.setXPosition(3);
        anotherMove.setYPosition(3);
        anotherMove.setMoveNumber(1);
        entityManager.persistAndFlush(anotherMove); // Move for anotherMatch

        // When
        List<Move> testMatchMoves = moveRepository.findByMatchIdOrderByMoveNumberAsc(testMatch.getId());
        List<Move> anotherMatchMoves = moveRepository.findByMatchIdOrderByMoveNumberAsc(anotherMatch.getId());

        // Then
        assertThat(testMatchMoves).hasSize(1);
        assertThat(testMatchMoves.get(0).getXPosition()).isEqualTo(5);
        
        assertThat(anotherMatchMoves).hasSize(1);
        assertThat(anotherMatchMoves.get(0).getXPosition()).isEqualTo(3);
    }
}
