package com.vn.caro_game.entities;

import com.vn.caro_game.enums.GameResult;
import com.vn.caro_game.enums.RoomStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GameMatch Entity Tests")
class GameMatchTest {

    private GameMatch gameMatch;
    private User playerX;
    private User playerO;
    private GameRoom room;

    @BeforeEach
    void setUp() {
        playerX = new User();
        playerX.setUsername("playerX");
        playerX.setEmail("playerX@example.com");
        playerX.setPassword("password");

        playerO = new User();
        playerO.setUsername("playerO");
        playerO.setEmail("playerO@example.com");
        playerO.setPassword("password");

        room = new GameRoom();
        room.setName("Test Room");
        room.setCreatedBy(playerX);
        room.setStatus(RoomStatus.PLAYING);

        gameMatch = new GameMatch();
        gameMatch.setRoom(room);
        gameMatch.setPlayerX(playerX);
        gameMatch.setPlayerO(playerO);
        gameMatch.setResult(GameResult.ONGOING);
    }

    @Test
    @DisplayName("Should create game match with valid properties")
    void shouldCreateGameMatchWithValidProperties() {
        // Then
        assertThat(gameMatch.getRoom()).isEqualTo(room);
        assertThat(gameMatch.getPlayerX()).isEqualTo(playerX);
        assertThat(gameMatch.getPlayerO()).isEqualTo(playerO);
        assertThat(gameMatch.getResult()).isEqualTo(GameResult.ONGOING);
    }

    @Test
    @DisplayName("Should support all game results")
    void shouldSupportAllGameResults() {
        // When & Then
        gameMatch.setResult(GameResult.ONGOING);
        assertThat(gameMatch.getResult()).isEqualTo(GameResult.ONGOING);

        gameMatch.setResult(GameResult.X_WIN);
        assertThat(gameMatch.getResult()).isEqualTo(GameResult.X_WIN);

        gameMatch.setResult(GameResult.O_WIN);
        assertThat(gameMatch.getResult()).isEqualTo(GameResult.O_WIN);

        gameMatch.setResult(GameResult.DRAW);
        assertThat(gameMatch.getResult()).isEqualTo(GameResult.DRAW);
    }

    @Test
    @DisplayName("Should handle player assignments correctly")
    void shouldHandlePlayerAssignmentsCorrectly() {
        // When
        User newPlayerX = new User();
        newPlayerX.setUsername("newPlayerX");
        newPlayerX.setEmail("newPlayerX@example.com");

        User newPlayerO = new User();
        newPlayerO.setUsername("newPlayerO");
        newPlayerO.setEmail("newPlayerO@example.com");

        gameMatch.setPlayerX(newPlayerX);
        gameMatch.setPlayerO(newPlayerO);

        // Then
        assertThat(gameMatch.getPlayerX()).isEqualTo(newPlayerX);
        assertThat(gameMatch.getPlayerO()).isEqualTo(newPlayerO);
        assertThat(gameMatch.getPlayerX().getUsername()).isEqualTo("newPlayerX");
        assertThat(gameMatch.getPlayerO().getUsername()).isEqualTo("newPlayerO");
    }

    @Test
    @DisplayName("Should handle null players")
    void shouldHandleNullPlayers() {
        // When
        gameMatch.setPlayerO(null);

        // Then
        assertThat(gameMatch.getPlayerX()).isEqualTo(playerX);
        assertThat(gameMatch.getPlayerO()).isNull();
    }

    @Test
    @DisplayName("Should initialize moves collection properly")
    void shouldInitializeMovesCollectionProperly() {
        // Then
        assertThat(gameMatch.getMoves()).isNotNull();
        assertThat(gameMatch.getMoves()).isEmpty();
    }

    @Test
    @DisplayName("GameResult enum should have correct values")
    void gameResultEnumShouldHaveCorrectValues() {
        // Then
        assertThat(GameResult.values()).containsExactlyInAnyOrder(
            GameResult.ONGOING,
            GameResult.X_WIN,
            GameResult.O_WIN,
            GameResult.DRAW
        );
    }

    @Test
    @DisplayName("Should handle different rooms")
    void shouldHandleDifferentRooms() {
        // Given
        GameRoom anotherRoom = new GameRoom();
        anotherRoom.setName("Another Room");
        anotherRoom.setCreatedBy(playerO);
        anotherRoom.setStatus(RoomStatus.PLAYING);

        // When
        gameMatch.setRoom(anotherRoom);

        // Then
        assertThat(gameMatch.getRoom()).isEqualTo(anotherRoom);
        assertThat(gameMatch.getRoom().getName()).isEqualTo("Another Room");
        assertThat(gameMatch.getRoom().getCreatedBy()).isEqualTo(playerO);
    }

    @Test
    @DisplayName("Should support same player as both X and O in different matches")
    void shouldSupportSamePlayerAsBothXAndOInDifferentMatches() {
        // Given
        GameMatch anotherMatch = new GameMatch();
        anotherMatch.setRoom(room);
        anotherMatch.setPlayerX(playerO); // playerO is now X
        anotherMatch.setPlayerO(playerX); // playerX is now O
        anotherMatch.setResult(GameResult.ONGOING);

        // Then
        assertThat(gameMatch.getPlayerX()).isEqualTo(playerX);
        assertThat(gameMatch.getPlayerO()).isEqualTo(playerO);
        
        assertThat(anotherMatch.getPlayerX()).isEqualTo(playerO);
        assertThat(anotherMatch.getPlayerO()).isEqualTo(playerX);
    }

    @Test
    @DisplayName("Should handle result changes during game progression")
    void shouldHandleResultChangesDuringGameProgression() {
        // Given - Game starts as ongoing
        assertThat(gameMatch.getResult()).isEqualTo(GameResult.ONGOING);

        // When - Game ends with X winning
        gameMatch.setResult(GameResult.X_WIN);

        // Then
        assertThat(gameMatch.getResult()).isEqualTo(GameResult.X_WIN);

        // When - Result is corrected to draw (hypothetical scenario)
        gameMatch.setResult(GameResult.DRAW);

        // Then
        assertThat(gameMatch.getResult()).isEqualTo(GameResult.DRAW);
    }

    @Test
    @DisplayName("Should maintain referential integrity with room")
    void shouldMaintainReferentialIntegrityWithRoom() {
        // Given
        GameRoom specificRoom = new GameRoom();
        specificRoom.setName("Specific Room");
        specificRoom.setCreatedBy(playerX);

        // When
        gameMatch.setRoom(specificRoom);

        // Then
        assertThat(gameMatch.getRoom()).isSameAs(specificRoom);
        assertThat(gameMatch.getRoom().getName()).isEqualTo("Specific Room");
    }

    @Test
    @DisplayName("Should handle match without room assignment")
    void shouldHandleMatchWithoutRoomAssignment() {
        // When
        gameMatch.setRoom(null);

        // Then
        assertThat(gameMatch.getRoom()).isNull();
        assertThat(gameMatch.getPlayerX()).isEqualTo(playerX);
        assertThat(gameMatch.getPlayerO()).isEqualTo(playerO);
    }
}
