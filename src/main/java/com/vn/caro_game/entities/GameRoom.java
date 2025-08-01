package com.vn.caro_game.entities;

import com.vn.caro_game.enums.GameState;
import com.vn.caro_game.enums.RematchState;
import com.vn.caro_game.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "game_rooms")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @Column(length = 100)
    String name;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    RoomStatus status = RoomStatus.WAITING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "game_state", length = 30, nullable = false)
    GameState gameState = GameState.WAITING_FOR_PLAYERS;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "rematch_state", length = 20, nullable = false)
    RematchState rematchState = RematchState.NONE;
    
    @Column(name = "rematch_requester_id")
    Long rematchRequesterId;
    
    @Column(name = "new_room_id")
    Long newRoomId;
    
    @Column(name = "game_started_at")
    LocalDateTime gameStartedAt;
    
    @Column(name = "game_ended_at")
    LocalDateTime gameEndedAt;
    
    @Column(name = "is_private", nullable = false)
    Boolean isPrivate = false;
    
    @Column(name = "join_code", length = 20)
    String joinCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    User createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
    
    // Relationships
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<RoomPlayer> roomPlayers = new HashSet<>();
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<GameMatch> gameMatches = new HashSet<>();
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<ChatMessage> chatMessages = new HashSet<>();
    
    // Helper methods for game state management
    public int getPlayerCount() {
        return roomPlayers.size();
    }
    
    public long getReadyPlayersCount() {
        return roomPlayers.stream()
                .filter(rp -> rp.getReadyState() == com.vn.caro_game.enums.PlayerReadyState.READY)
                .count();
    }
    
    public boolean bothPlayersReady() {
        return getPlayerCount() >= 2 && getReadyPlayersCount() >= 2;
    }
    
    public boolean canStartGame() {
        return gameState == GameState.WAITING_FOR_READY && bothPlayersReady();
    }
    
    public boolean canRematch() {
        // Allow rematch for normal finish and surrender, but NOT for leave
        return (gameState == GameState.FINISHED || 
                gameState == GameState.ENDED_BY_SURRENDER) && 
               getPlayerCount() == 2;
    }
    
    public boolean isGameActive() {
        return gameState == GameState.IN_PROGRESS;
    }
}
