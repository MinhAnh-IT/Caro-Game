package com.vn.caro_game.entities;

import com.vn.caro_game.enums.GameEndReason;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_history")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @Column(name = "room_id", nullable = false)
    Long roomId;
    
    @Column(name = "winner_id")
    Long winnerId;
    
    @Column(name = "loser_id")
    Long loserId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "end_reason", nullable = false)
    GameEndReason endReason;
    
    @Column(name = "game_started_at")
    LocalDateTime gameStartedAt;
    
    @Column(name = "game_ended_at")
    LocalDateTime gameEndedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id", insertable = false, updatable = false)
    User winner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loser_id", insertable = false, updatable = false)
    User loser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    GameRoom room;
}
