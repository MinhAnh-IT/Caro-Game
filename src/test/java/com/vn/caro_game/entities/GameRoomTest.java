package com.vn.caro_game.entities;

import com.vn.caro_game.enums.RoomStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GameRoom Entity Tests")
class GameRoomTest {

    private GameRoom gameRoom;
    private User creator;

    @BeforeEach
    void setUp() {
        creator = new User();
        creator.setUsername("creator");
        creator.setEmail("creator@example.com");
        creator.setPassword("password");

        gameRoom = new GameRoom();
        gameRoom.setName("Test Room");
        gameRoom.setCreatedBy(creator);
        gameRoom.setStatus(RoomStatus.WAITING);
        gameRoom.setIsPrivate(false);
        gameRoom.setJoinCode("ABC123");
    }

    @Test
    @DisplayName("Should create game room with valid properties")
    void shouldCreateGameRoomWithValidProperties() {
        // Then
        assertThat(gameRoom.getName()).isEqualTo("Test Room");
        assertThat(gameRoom.getCreatedBy()).isEqualTo(creator);
        assertThat(gameRoom.getStatus()).isEqualTo(RoomStatus.WAITING);
        assertThat(gameRoom.getIsPrivate()).isFalse();
        assertThat(gameRoom.getJoinCode()).isEqualTo("ABC123");
    }

    @Test
    @DisplayName("Should support all room statuses")
    void shouldSupportAllRoomStatuses() {
        // When & Then
        gameRoom.setStatus(RoomStatus.WAITING);
        assertThat(gameRoom.getStatus()).isEqualTo(RoomStatus.WAITING);

        gameRoom.setStatus(RoomStatus.PLAYING);
        assertThat(gameRoom.getStatus()).isEqualTo(RoomStatus.PLAYING);

        gameRoom.setStatus(RoomStatus.FINISHED);
        assertThat(gameRoom.getStatus()).isEqualTo(RoomStatus.FINISHED);
    }

    @Test
    @DisplayName("Should handle private room settings")
    void shouldHandlePrivateRoomSettings() {
        // When
        gameRoom.setIsPrivate(true);
        gameRoom.setJoinCode("SECRET123");

        // Then
        assertThat(gameRoom.getIsPrivate()).isTrue();
        assertThat(gameRoom.getJoinCode()).isEqualTo("SECRET123");
    }

    @Test
    @DisplayName("Should handle public room settings")
    void shouldHandlePublicRoomSettings() {
        // When
        gameRoom.setIsPrivate(false);
        gameRoom.setJoinCode(null);

        // Then
        assertThat(gameRoom.getIsPrivate()).isFalse();
        assertThat(gameRoom.getJoinCode()).isNull();
    }

    @Test
    @DisplayName("Should initialize collections properly")
    void shouldInitializeCollectionsProperly() {
        // Then
        assertThat(gameRoom.getRoomPlayers()).isNotNull();
        assertThat(gameRoom.getRoomPlayers()).isEmpty();
        assertThat(gameRoom.getGameMatches()).isNotNull();
        assertThat(gameRoom.getGameMatches()).isEmpty();
        assertThat(gameRoom.getChatMessages()).isNotNull();
        assertThat(gameRoom.getChatMessages()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null join code")
    void shouldHandleNullJoinCode() {
        // When
        gameRoom.setJoinCode(null);

        // Then
        assertThat(gameRoom.getJoinCode()).isNull();
    }

    @Test
    @DisplayName("Should handle empty room name")
    void shouldHandleEmptyRoomName() {
        // When
        gameRoom.setName("");

        // Then
        assertThat(gameRoom.getName()).isEqualTo("");
    }

    @Test
    @DisplayName("Should handle different creators")
    void shouldHandleDifferentCreators() {
        // Given
        User anotherCreator = new User();
        anotherCreator.setUsername("another");
        anotherCreator.setEmail("another@example.com");
        anotherCreator.setPassword("password");

        // When
        gameRoom.setCreatedBy(anotherCreator);

        // Then
        assertThat(gameRoom.getCreatedBy()).isEqualTo(anotherCreator);
        assertThat(gameRoom.getCreatedBy().getUsername()).isEqualTo("another");
    }

    @Test
    @DisplayName("RoomStatus enum should have correct values")
    void roomStatusEnumShouldHaveCorrectValues() {
        // Then
        assertThat(RoomStatus.values()).containsExactly(
            RoomStatus.WAITING,
            RoomStatus.PLAYING,
            RoomStatus.FINISHED
        );
    }

    @Test
    @DisplayName("Should support long room names")
    void shouldSupportLongRoomNames() {
        // Given
        String longName = "A".repeat(100); // 100 characters

        // When
        gameRoom.setName(longName);

        // Then
        assertThat(gameRoom.getName()).isEqualTo(longName);
        assertThat(gameRoom.getName().length()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should support join code with different formats")
    void shouldSupportJoinCodeWithDifferentFormats() {
        // When & Then
        gameRoom.setJoinCode("123456");
        assertThat(gameRoom.getJoinCode()).isEqualTo("123456");

        gameRoom.setJoinCode("ABC-123");
        assertThat(gameRoom.getJoinCode()).isEqualTo("ABC-123");

        gameRoom.setJoinCode("a1b2c3");
        assertThat(gameRoom.getJoinCode()).isEqualTo("a1b2c3");
    }
}
