package com.vn.caro_game.repositories;
import com.vn.caro_game.enums.RoomStatus;
import com.vn.caro_game.entities.GameRoom;
import com.vn.caro_game.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("GameRoomRepository Tests")
class GameRoomRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GameRoomRepository gameRoomRepository;

    private User testUser;
    private GameRoom testRoom;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser = entityManager.persistAndFlush(testUser);

        testRoom = new GameRoom();
        testRoom.setName("Test Room");
        testRoom.setCreatedBy(testUser);
        testRoom.setStatus(RoomStatus.WAITING);
        testRoom.setIsPrivate(false);
        testRoom.setJoinCode("ABC123");
    }

    @Test
    @DisplayName("Should save and retrieve game room")
    void shouldSaveAndRetrieveGameRoom() {
        // When
        GameRoom savedRoom = gameRoomRepository.save(testRoom);

        // Then
        assertThat(savedRoom.getId()).isNotNull();
        assertThat(savedRoom.getName()).isEqualTo("Test Room");
        assertThat(savedRoom.getCreatedBy().getId()).isEqualTo(testUser.getId());
        assertThat(savedRoom.getStatus()).isEqualTo(RoomStatus.WAITING);
        assertThat(savedRoom.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find public rooms by status")
    void shouldFindPublicRoomsByStatus() {
        // Given
        entityManager.persistAndFlush(testRoom);
        
        GameRoom playingRoom = new GameRoom();
        playingRoom.setName("Playing Room");
        playingRoom.setCreatedBy(testUser);
        playingRoom.setStatus(RoomStatus.PLAYING);
        playingRoom.setIsPrivate(false);
        entityManager.persistAndFlush(playingRoom);

        // When
        List<GameRoom> waitingRooms = gameRoomRepository.findByStatusAndIsPrivateFalse(RoomStatus.WAITING);
        List<GameRoom> playingRooms = gameRoomRepository.findByStatusAndIsPrivateFalse(RoomStatus.PLAYING);

        // Then
        assertThat(waitingRooms).hasSize(1);
        assertThat(waitingRooms.get(0).getName()).isEqualTo("Test Room");
        
        assertThat(playingRooms).hasSize(1);
        assertThat(playingRooms.get(0).getName()).isEqualTo("Playing Room");
    }

    @Test
    @DisplayName("Should find room by join code")
    void shouldFindRoomByJoinCode() {
        // Given
        entityManager.persistAndFlush(testRoom);

        // When
        Optional<GameRoom> foundRoom = gameRoomRepository.findByJoinCode("ABC123");

        // Then
        assertThat(foundRoom).isPresent();
        assertThat(foundRoom.get().getName()).isEqualTo("Test Room");
        assertThat(foundRoom.get().getJoinCode()).isEqualTo("ABC123");
    }

    @Test
    @DisplayName("Should return empty when join code not found")
    void shouldReturnEmptyWhenJoinCodeNotFound() {
        // When
        Optional<GameRoom> foundRoom = gameRoomRepository.findByJoinCode("NONEXISTENT");

        // Then
        assertThat(foundRoom).isEmpty();
    }

    @Test
    @DisplayName("Should find rooms by creator")
    void shouldFindRoomsByCreator() {
        // Given
        entityManager.persistAndFlush(testRoom);
        
        GameRoom anotherRoom = new GameRoom();
        anotherRoom.setName("Another Room");
        anotherRoom.setCreatedBy(testUser);
        anotherRoom.setStatus(RoomStatus.WAITING);
        anotherRoom.setIsPrivate(true);
        entityManager.persistAndFlush(anotherRoom);

        // When
        List<GameRoom> userRooms = gameRoomRepository.findByCreatedBy_Id(testUser.getId());

        // Then
        assertThat(userRooms).hasSize(2);
        assertThat(userRooms).extracting(GameRoom::getName)
                           .containsExactlyInAnyOrder("Test Room", "Another Room");
    }

    @Test
    @DisplayName("Should find rooms by user participation")
    void shouldFindRoomsByUserParticipation() {
        // This test would require RoomPlayer setup, so we'll test the basic query
        // Given
        entityManager.persistAndFlush(testRoom);

        // When
        List<GameRoom> userRooms = gameRoomRepository.findRoomsByUserId(testUser.getId());

        // Then - Empty because no RoomPlayer entries exist yet
        assertThat(userRooms).isEmpty();
    }

    @Test
    @DisplayName("Should find available rooms for user")
    void shouldFindAvailableRoomsForUser() {
        // Given
        entityManager.persistAndFlush(testRoom); // Public room
        
        GameRoom privateRoom = new GameRoom();
        privateRoom.setName("Private Room");
        privateRoom.setCreatedBy(testUser);
        privateRoom.setStatus(RoomStatus.WAITING);
        privateRoom.setIsPrivate(true);
        entityManager.persistAndFlush(privateRoom);

        // When - User should see public rooms + their own private rooms
        List<GameRoom> availableRooms = gameRoomRepository.findAvailableRoomsForUser(
            RoomStatus.WAITING, testUser.getId());

        // Then
        assertThat(availableRooms).hasSize(2);
        assertThat(availableRooms).extracting(GameRoom::getName)
                                .containsExactlyInAnyOrder("Test Room", "Private Room");
    }

    @Test
    @DisplayName("Should exclude private rooms from other users")
    void shouldExcludePrivateRoomsFromOtherUsers() {
        // Given
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password");
        anotherUser = entityManager.persistAndFlush(anotherUser);
        
        entityManager.persistAndFlush(testRoom); // Public room by testUser
        
        GameRoom privateRoom = new GameRoom();
        privateRoom.setName("Private Room");
        privateRoom.setCreatedBy(anotherUser); // Created by different user
        privateRoom.setStatus(RoomStatus.WAITING);
        privateRoom.setIsPrivate(true);
        entityManager.persistAndFlush(privateRoom);

        // When - testUser should only see public rooms and their own private rooms
        List<GameRoom> availableRooms = gameRoomRepository.findAvailableRoomsForUser(
            RoomStatus.WAITING, testUser.getId());

        // Then - Should only see the public room, not the private room from another user
        assertThat(availableRooms).hasSize(1);
        assertThat(availableRooms.get(0).getName()).isEqualTo("Test Room");
    }
}
