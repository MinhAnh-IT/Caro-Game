package com.vn.caro_game.entities;

import com.vn.caro_game.enums.RoomStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RoomPlayer Entity Tests")
class RoomPlayerTest {

    private User testUser;
    private GameRoom testRoom;
    private RoomPlayer roomPlayer;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");

        testRoom = new GameRoom();
        testRoom.setId(1L);
        testRoom.setName("Test Room");
        testRoom.setCreatedBy(testUser);
        testRoom.setStatus(RoomStatus.WAITING);
        testRoom.setIsPrivate(false);

        roomPlayer = new RoomPlayer();
        RoomPlayer.RoomPlayerId id = new RoomPlayer.RoomPlayerId();
        id.setRoomId(testRoom.getId());
        id.setUserId(testUser.getId());
        roomPlayer.setId(id);
        roomPlayer.setRoom(testRoom);
        roomPlayer.setUser(testUser);
        roomPlayer.setIsHost(true);
        roomPlayer.setJoinTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create RoomPlayer with all required fields")
    void shouldCreateRoomPlayerWithAllRequiredFields() {
        assertThat(roomPlayer).isNotNull();
        assertThat(roomPlayer.getId()).isNotNull();
        assertThat(roomPlayer.getId().getRoomId()).isEqualTo(1L);
        assertThat(roomPlayer.getId().getUserId()).isEqualTo(1L);
        assertThat(roomPlayer.getRoom()).isEqualTo(testRoom);
        assertThat(roomPlayer.getUser()).isEqualTo(testUser);
        assertThat(roomPlayer.getIsHost()).isTrue();
        assertThat(roomPlayer.getJoinTime()).isNotNull();
    }

    @Test
    @DisplayName("Should handle composite key properly")
    void shouldHandleCompositeKeyProperly() {
        RoomPlayer.RoomPlayerId compositeId = roomPlayer.getId();
        
        assertThat(compositeId).isNotNull();
        assertThat(compositeId.getRoomId()).isEqualTo(testRoom.getId());
        assertThat(compositeId.getUserId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Should set and get host status")
    void shouldSetAndGetHostStatus() {
        roomPlayer.setIsHost(false);
        assertThat(roomPlayer.getIsHost()).isFalse();

        roomPlayer.setIsHost(true);
        assertThat(roomPlayer.getIsHost()).isTrue();
    }

    @Test
    @DisplayName("Should handle join time")
    void shouldHandleJoinTime() {
        LocalDateTime joinTime = LocalDateTime.of(2024, 1, 1, 12, 0);
        roomPlayer.setJoinTime(joinTime);
        
        assertThat(roomPlayer.getJoinTime()).isEqualTo(joinTime);
    }

    @Test
    @DisplayName("Should maintain relationships with User and GameRoom")
    void shouldMaintainRelationshipsWithUserAndGameRoom() {
        assertThat(roomPlayer.getUser()).isEqualTo(testUser);
        assertThat(roomPlayer.getRoom()).isEqualTo(testRoom);
        
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        
        roomPlayer.setUser(newUser);
        assertThat(roomPlayer.getUser()).isEqualTo(newUser);
    }

    @Test
    @DisplayName("Should handle null values appropriately")
    void shouldHandleNullValuesAppropriately() {
        RoomPlayer newRoomPlayer = new RoomPlayer();
        
        assertThat(newRoomPlayer.getId()).isNull();
        assertThat(newRoomPlayer.getUser()).isNull();
        assertThat(newRoomPlayer.getRoom()).isNull();
        assertThat(newRoomPlayer.getIsHost()).isFalse(); // Default value is false
        assertThat(newRoomPlayer.getJoinTime()).isNull();
    }

    @Test
    @DisplayName("Should validate composite key equality")
    void shouldValidateCompositeKeyEquality() {
        RoomPlayer.RoomPlayerId id1 = new RoomPlayer.RoomPlayerId();
        id1.setRoomId(1L);
        id1.setUserId(1L);
        
        RoomPlayer.RoomPlayerId id2 = new RoomPlayer.RoomPlayerId();
        id2.setRoomId(1L);
        id2.setUserId(1L);
        
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("Should validate composite key inequality")
    void shouldValidateCompositeKeyInequality() {
        RoomPlayer.RoomPlayerId id1 = new RoomPlayer.RoomPlayerId();
        id1.setRoomId(1L);
        id1.setUserId(1L);
        
        RoomPlayer.RoomPlayerId id2 = new RoomPlayer.RoomPlayerId();
        id2.setRoomId(2L);
        id2.setUserId(1L);
        
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("Should update composite key when entity relationships change")
    void shouldUpdateCompositeKeyWhenEntityRelationshipsChange() {
        User newUser = new User();
        newUser.setId(2L);
        
        GameRoom newRoom = new GameRoom();
        newRoom.setId(2L);
        
        RoomPlayer.RoomPlayerId newId = new RoomPlayer.RoomPlayerId();
        newId.setRoomId(newRoom.getId());
        newId.setUserId(newUser.getId());
        
        roomPlayer.setId(newId);
        roomPlayer.setUser(newUser);
        roomPlayer.setRoom(newRoom);
        
        assertThat(roomPlayer.getId().getRoomId()).isEqualTo(2L);
        assertThat(roomPlayer.getId().getUserId()).isEqualTo(2L);
        assertThat(roomPlayer.getUser()).isEqualTo(newUser);
        assertThat(roomPlayer.getRoom()).isEqualTo(newRoom);
    }

    @Test
    @DisplayName("Should handle boolean host flag properly")
    void shouldHandleBooleanHostFlagProperly() {
        // Test default state
        RoomPlayer newRoomPlayer = new RoomPlayer();
        assertThat(newRoomPlayer.getIsHost()).isFalse(); // Default value is false
        
        // Test setting to false
        newRoomPlayer.setIsHost(false);
        assertThat(newRoomPlayer.getIsHost()).isFalse();
        
        // Test setting to true
        newRoomPlayer.setIsHost(true);
        assertThat(newRoomPlayer.getIsHost()).isTrue();
    }

    @Test
    @DisplayName("Should maintain temporal consistency with join time")
    void shouldMaintainTemporalConsistencyWithJoinTime() {
        LocalDateTime before = LocalDateTime.now().minusMinutes(1);
        LocalDateTime joinTime = LocalDateTime.now();
        LocalDateTime after = LocalDateTime.now().plusMinutes(1);
        
        roomPlayer.setJoinTime(joinTime);
        
        assertThat(roomPlayer.getJoinTime()).isAfter(before);
        assertThat(roomPlayer.getJoinTime()).isBefore(after);
        assertThat(roomPlayer.getJoinTime()).isEqualTo(joinTime);
    }
}
