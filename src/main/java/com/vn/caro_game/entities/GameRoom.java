package com.vn.caro_game.entities;

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
}
