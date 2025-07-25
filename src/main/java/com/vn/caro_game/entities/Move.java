package com.vn.caro_game.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "moves")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Move {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    GameMatch match;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    User player;
    
    @Column(name = "x_position", nullable = false)
    Integer xPosition;
    
    @Column(name = "y_position", nullable = false)
    Integer yPosition;
    
    @Column(name = "move_number", nullable = false)
    Integer moveNumber;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
}
