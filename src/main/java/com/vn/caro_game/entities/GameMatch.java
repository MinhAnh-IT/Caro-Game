package com.vn.caro_game.entities;

import com.vn.caro_game.enums.GameResult;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "game_matches")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    GameRoom room;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_x_id")
    User playerX;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_o_id")
    User playerO;
    
    @Column(name = "start_time")
    LocalDateTime startTime;
    
    @Column(name = "end_time")
    LocalDateTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    GameResult result;
    
    // Relationships
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Move> moves = new HashSet<>();
}
