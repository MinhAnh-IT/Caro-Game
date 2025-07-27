package com.vn.caro_game.repositories;
import com.vn.caro_game.enums.RoomStatus;
import com.vn.caro_game.entities.GameRoom;
import com.vn.caro_game.entities.RoomPlayer;
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
@DisplayName("RoomPlayerRepository Tests")
class RoomPlayerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoomPlayerRepository roomPlayerRepository;

    private User testUser;
    private User anotherUser;
    private GameRoom testRoom;
    private RoomPlayer testRoomPlayer;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser = entityManager.persistAndFlush(testUser);

        anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("hashedPassword");
        anotherUser = entityManager.persistAndFlush(anotherUser);

        testRoom = new GameRoom();
        testRoom.setName("Test Room");
        testRoom.setCreatedBy(testUser);
        testRoom.setStatus(RoomStatus.WAITING);
        testRoom.setIsPrivate(false);
        testRoom.setJoinCode("ABC123");
        testRoom = entityManager.persistAndFlush(testRoom);

        testRoomPlayer = new RoomPlayer();
        RoomPlayer.RoomPlayerId id = new RoomPlayer.RoomPlayerId();
        id.setRoomId(testRoom.getId());
        id.setUserId(testUser.getId());
        testRoomPlayer.setId(id);
        testRoomPlayer.setRoom(testRoom);
        testRoomPlayer.setUser(testUser);
        testRoomPlayer.setIsHost(true);
    }

    @Test
    @DisplayName("Should save and retrieve room player")
    void shouldSaveAndRetrieveRoomPlayer() {
        // When
        RoomPlayer savedRoomPlayer = entityManager.persistAndFlush(testRoomPlayer);

        // Then
        assertThat(savedRoomPlayer).isNotNull();
        assertThat(savedRoomPlayer.getId()).isNotNull();
        assertThat(savedRoomPlayer.getId().getRoomId()).isEqualTo(testRoom.getId());
        assertThat(savedRoomPlayer.getId().getUserId()).isEqualTo(testUser.getId());
        assertThat(savedRoomPlayer.getUser()).isNotNull();
        assertThat(savedRoomPlayer.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(savedRoomPlayer.getRoom()).isNotNull();
        assertThat(savedRoomPlayer.getRoom().getId()).isEqualTo(testRoom.getId());
        assertThat(savedRoomPlayer.getIsHost()).isTrue();
        assertThat(savedRoomPlayer.getJoinTime()).isNotNull();
    }

    @Test
    @DisplayName("Should find players by room ID")
    void shouldFindPlayersByRoomId() {
        // Given
        entityManager.persistAndFlush(testRoomPlayer);
        
        RoomPlayer anotherRoomPlayer = new RoomPlayer();
        RoomPlayer.RoomPlayerId anotherId = new RoomPlayer.RoomPlayerId();
        anotherId.setRoomId(testRoom.getId());
        anotherId.setUserId(anotherUser.getId());
        anotherRoomPlayer.setId(anotherId);
        anotherRoomPlayer.setRoom(testRoom);
        anotherRoomPlayer.setUser(anotherUser);
        anotherRoomPlayer.setIsHost(false);
        entityManager.persistAndFlush(anotherRoomPlayer);

        // When
        List<RoomPlayer> roomPlayers = roomPlayerRepository.findByRoomId(testRoom.getId());

        // Then
        assertThat(roomPlayers).hasSize(2);
        assertThat(roomPlayers).extracting(rp -> rp.getUser().getUsername())
                              .containsExactlyInAnyOrder("testuser", "anotheruser");
    }

    @Test
    @DisplayName("Should find rooms by user ID")
    void shouldFindRoomsByUserId() {
        // Given
        entityManager.persistAndFlush(testRoomPlayer);
        
        GameRoom anotherRoom = new GameRoom();
        anotherRoom.setName("Another Room");
        anotherRoom.setCreatedBy(anotherUser);
        anotherRoom.setStatus(RoomStatus.WAITING);
        anotherRoom.setIsPrivate(false);
        anotherRoom = entityManager.persistAndFlush(anotherRoom);
        
        RoomPlayer anotherRoomPlayer = new RoomPlayer();
        RoomPlayer.RoomPlayerId anotherId = new RoomPlayer.RoomPlayerId();
        anotherId.setRoomId(anotherRoom.getId());
        anotherId.setUserId(testUser.getId());
        anotherRoomPlayer.setId(anotherId);
        anotherRoomPlayer.setRoom(anotherRoom);
        anotherRoomPlayer.setUser(testUser);
        anotherRoomPlayer.setIsHost(false);
        entityManager.persistAndFlush(anotherRoomPlayer);

        // When
        List<RoomPlayer> userRoomPlayers = roomPlayerRepository.findByUserId(testUser.getId());

        // Then
        assertThat(userRoomPlayers).hasSize(2);
        assertThat(userRoomPlayers).extracting(rp -> rp.getRoom().getName())
                                 .containsExactlyInAnyOrder("Test Room", "Another Room");
    }

    @Test
    @DisplayName("Should find host by room ID")
    void shouldFindHostByRoomId() {
        // Given
        entityManager.persistAndFlush(testRoomPlayer);
        
        RoomPlayer nonHostPlayer = new RoomPlayer();
        RoomPlayer.RoomPlayerId nonHostId = new RoomPlayer.RoomPlayerId();
        nonHostId.setRoomId(testRoom.getId());
        nonHostId.setUserId(anotherUser.getId());
        nonHostPlayer.setId(nonHostId);
        nonHostPlayer.setRoom(testRoom);
        nonHostPlayer.setUser(anotherUser);
        nonHostPlayer.setIsHost(false);
        entityManager.persistAndFlush(nonHostPlayer);

        // When
        Optional<RoomPlayer> host = roomPlayerRepository.findHostByRoomId(testRoom.getId());

        // Then
        assertThat(host).isPresent();
        assertThat(host.get().getUser().getUsername()).isEqualTo("testuser");
        assertThat(host.get().getIsHost()).isTrue();
    }

    @Test
    @DisplayName("Should return empty when no host found")
    void shouldReturnEmptyWhenNoHostFound() {
        // Given - Create room player but not as host
        testRoomPlayer.setIsHost(false);
        entityManager.persistAndFlush(testRoomPlayer);

        // When
        Optional<RoomPlayer> host = roomPlayerRepository.findHostByRoomId(testRoom.getId());

        // Then
        assertThat(host).isEmpty();
    }

    @Test
    @DisplayName("Should count players by room ID")
    void shouldCountPlayersByRoomId() {
        // Given
        entityManager.persistAndFlush(testRoomPlayer);
        
        RoomPlayer anotherRoomPlayer = new RoomPlayer();
        RoomPlayer.RoomPlayerId anotherId = new RoomPlayer.RoomPlayerId();
        anotherId.setRoomId(testRoom.getId());
        anotherId.setUserId(anotherUser.getId());
        anotherRoomPlayer.setId(anotherId);
        anotherRoomPlayer.setRoom(testRoom);
        anotherRoomPlayer.setUser(anotherUser);
        anotherRoomPlayer.setIsHost(false);
        entityManager.persistAndFlush(anotherRoomPlayer);

        // When
        long playerCount = roomPlayerRepository.countPlayersByRoomId(testRoom.getId());

        // Then
        assertThat(playerCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should check if user exists in room")
    void shouldCheckIfUserExistsInRoom() {
        // Given
        entityManager.persistAndFlush(testRoomPlayer);

        // When & Then
        assertThat(roomPlayerRepository.existsByRoomIdAndUserId(testRoom.getId(), testUser.getId())).isTrue();
        assertThat(roomPlayerRepository.existsByRoomIdAndUserId(testRoom.getId(), anotherUser.getId())).isFalse();
    }

    @Test
    @DisplayName("Should return empty list when room has no players")
    void shouldReturnEmptyListWhenRoomHasNoPlayers() {
        // When
        List<RoomPlayer> roomPlayers = roomPlayerRepository.findByRoomId(testRoom.getId());

        // Then
        assertThat(roomPlayers).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when user is not in any room")
    void shouldReturnEmptyListWhenUserNotInAnyRoom() {
        // When
        List<RoomPlayer> userRoomPlayers = roomPlayerRepository.findByUserId(anotherUser.getId());

        // Then
        assertThat(userRoomPlayers).isEmpty();
    }
}
