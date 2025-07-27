package com.vn.caro_game.repositories;
import com.vn.caro_game.enums.GameResult;import com.vn.caro_game.enums.RoomStatus;
import com.vn.caro_game.entities.GameMatch;
import com.vn.caro_game.entities.GameRoom;
import com.vn.caro_game.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("GameMatchRepository Tests")
class GameMatchRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GameMatchRepository gameMatchRepository;

    private User playerX;
    private User playerO;
    private GameRoom testRoom;
    private GameMatch testMatch;

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
        testMatch.setStartTime(LocalDateTime.now()); // Ensure startTime is set
    }

    @Test
    @DisplayName("Should save and retrieve game match")
    void shouldSaveAndRetrieveGameMatch() {
        // When - save the testMatch that was set up in @BeforeEach
        GameMatch savedMatch = entityManager.persistAndFlush(testMatch);
        Long savedId = savedMatch.getId();
        entityManager.clear(); // Clear persistence context

        // Then - retrieve from database to verify persistence
        GameMatch foundMatch = gameMatchRepository.findById(savedId).orElse(null);
        assertThat(foundMatch).isNotNull();
        assertThat(foundMatch.getId()).isEqualTo(savedId);
        assertThat(foundMatch.getRoom().getId()).isEqualTo(testRoom.getId());
        assertThat(foundMatch.getPlayerX().getId()).isEqualTo(playerX.getId());
        assertThat(foundMatch.getPlayerO().getId()).isEqualTo(playerO.getId());
        assertThat(foundMatch.getResult()).isEqualTo(GameResult.ONGOING);
        assertThat(foundMatch.getStartTime()).isNotNull();
    }

    @Test
    @DisplayName("Should find matches by room ID")
    void shouldFindMatchesByRoomId() {
        // Given
        entityManager.persistAndFlush(testMatch);
        
        GameMatch anotherMatch = new GameMatch();
        anotherMatch.setRoom(testRoom);
        anotherMatch.setPlayerX(playerO);
        anotherMatch.setPlayerO(playerX);
        anotherMatch.setResult(GameResult.X_WIN);
        entityManager.persistAndFlush(anotherMatch);

        // When
        List<GameMatch> roomMatches = gameMatchRepository.findByRoomId(testRoom.getId());

        // Then
        assertThat(roomMatches).hasSize(2);
        assertThat(roomMatches).extracting(GameMatch::getResult)
                              .containsExactlyInAnyOrder(GameResult.ONGOING, GameResult.X_WIN);
    }

    @Test
    @DisplayName("Should find matches by player ID")
    void shouldFindMatchesByPlayerId() {
        // Given
        entityManager.persistAndFlush(testMatch);
        
        User anotherPlayer = new User();
        anotherPlayer.setUsername("anotherPlayer");
        anotherPlayer.setEmail("another@example.com");
        anotherPlayer.setPassword("password");
        anotherPlayer = entityManager.persistAndFlush(anotherPlayer);
        
        GameMatch anotherMatch = new GameMatch();
        anotherMatch.setRoom(testRoom);
        anotherMatch.setPlayerX(anotherPlayer);
        anotherMatch.setPlayerO(playerX); // playerX participates
        anotherMatch.setResult(GameResult.O_WIN);
        entityManager.persistAndFlush(anotherMatch);

        // When
        List<GameMatch> playerXMatches = gameMatchRepository.findByPlayerId(playerX.getId());

        // Then
        assertThat(playerXMatches).hasSize(2); // playerX is in both matches
        assertThat(playerXMatches).extracting(GameMatch::getResult)
                                 .containsExactlyInAnyOrder(GameResult.ONGOING, GameResult.O_WIN);
    }

    @Test
    @DisplayName("Should find match by room ID and result")
    void shouldFindMatchByRoomIdAndResult() {
        // Given
        testMatch.setResult(GameResult.X_WIN);
        entityManager.persistAndFlush(testMatch);

        // When
        Optional<GameMatch> foundMatch = gameMatchRepository.findByRoomIdAndResult(
            testRoom.getId(), GameResult.X_WIN);

        // Then
        assertThat(foundMatch).isPresent();
        assertThat(foundMatch.get().getPlayerX().getId()).isEqualTo(playerX.getId());
        assertThat(foundMatch.get().getResult()).isEqualTo(GameResult.X_WIN);
    }

    @Test
    @DisplayName("Should return empty when no match with specific result found")
    void shouldReturnEmptyWhenNoMatchWithSpecificResultFound() {
        // Given
        entityManager.persistAndFlush(testMatch); // ONGOING result

        // When
        Optional<GameMatch> foundMatch = gameMatchRepository.findByRoomIdAndResult(
            testRoom.getId(), GameResult.DRAW);

        // Then
        assertThat(foundMatch).isEmpty();
    }

    @Test
    @DisplayName("Should find matches by player ID and result")
    void shouldFindMatchesByPlayerIdAndResult() {
        // Given
        testMatch.setResult(GameResult.X_WIN);
        entityManager.persistAndFlush(testMatch);
        
        GameMatch drawMatch = new GameMatch();
        drawMatch.setRoom(testRoom);
        drawMatch.setPlayerX(playerX);
        drawMatch.setPlayerO(playerO);
        drawMatch.setResult(GameResult.DRAW);
        entityManager.persistAndFlush(drawMatch);
        
        GameMatch loseMatch = new GameMatch();
        loseMatch.setRoom(testRoom);
        loseMatch.setPlayerX(playerX);
        loseMatch.setPlayerO(playerO);
        loseMatch.setResult(GameResult.O_WIN);
        entityManager.persistAndFlush(loseMatch);

        // When
        List<GameMatch> winMatches = gameMatchRepository.findByPlayerIdAndResult(
            playerX.getId(), GameResult.X_WIN);
        List<GameMatch> drawMatches = gameMatchRepository.findByPlayerIdAndResult(
            playerX.getId(), GameResult.DRAW);

        // Then
        assertThat(winMatches).hasSize(1);
        assertThat(winMatches.get(0).getResult()).isEqualTo(GameResult.X_WIN);
        
        assertThat(drawMatches).hasSize(1);
        assertThat(drawMatches.get(0).getResult()).isEqualTo(GameResult.DRAW);
    }

    @Test
    @DisplayName("Should count wins by player X")
    void shouldCountWinsByPlayerX() {
        // Given
        testMatch.setResult(GameResult.X_WIN);
        entityManager.persistAndFlush(testMatch);
        
        GameMatch anotherWin = new GameMatch();
        anotherWin.setRoom(testRoom);
        anotherWin.setPlayerX(playerX);
        anotherWin.setPlayerO(playerO);
        anotherWin.setResult(GameResult.X_WIN);
        entityManager.persistAndFlush(anotherWin);
        
        GameMatch loseMatch = new GameMatch();
        loseMatch.setRoom(testRoom);
        loseMatch.setPlayerX(playerX);
        loseMatch.setPlayerO(playerO);
        loseMatch.setResult(GameResult.O_WIN);
        entityManager.persistAndFlush(loseMatch);

        // When
        long winCount = gameMatchRepository.countWinsByPlayerX(playerX.getId());

        // Then
        assertThat(winCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should count wins by player O")
    void shouldCountWinsByPlayerO() {
        // Given
        testMatch.setResult(GameResult.O_WIN);
        entityManager.persistAndFlush(testMatch);
        
        GameMatch anotherMatch = new GameMatch();
        anotherMatch.setRoom(testRoom);
        anotherMatch.setPlayerX(playerX);
        anotherMatch.setPlayerO(playerO);
        anotherMatch.setResult(GameResult.X_WIN);
        entityManager.persistAndFlush(anotherMatch);

        // When
        long winCount = gameMatchRepository.countWinsByPlayerO(playerO.getId());

        // Then
        assertThat(winCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return empty list when room has no matches")
    void shouldReturnEmptyListWhenRoomHasNoMatches() {
        // When
        List<GameMatch> roomMatches = gameMatchRepository.findByRoomId(testRoom.getId());

        // Then
        assertThat(roomMatches).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when player has no matches")
    void shouldReturnEmptyListWhenPlayerHasNoMatches() {
        // Given
        User newPlayer = new User();
        newPlayer.setUsername("newPlayer");
        newPlayer.setEmail("new@example.com");
        newPlayer.setPassword("password");
        newPlayer = entityManager.persistAndFlush(newPlayer);

        // When
        List<GameMatch> playerMatches = gameMatchRepository.findByPlayerId(newPlayer.getId());

        // Then
        assertThat(playerMatches).isEmpty();
    }
}
