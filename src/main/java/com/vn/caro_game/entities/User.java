package com.vn.caro_game.entities;

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
@Table(name = "users")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @Column(unique = true, nullable = false)
    String username;
    
    @Column(name = "password_hash", nullable = false)
    String password;
    
    @Column(unique = true, nullable = false)
    String email;
    
    @Column(name = "avatar_url")
    String avatarUrl;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
    
    // Relationships
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<GameRoom> createdRooms = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<RoomPlayer> roomParticipations = new HashSet<>();
    
    @OneToMany(mappedBy = "playerX", cascade = CascadeType.ALL)
    Set<GameMatch> matchesAsPlayerX = new HashSet<>();
    
    @OneToMany(mappedBy = "playerO", cascade = CascadeType.ALL)
    Set<GameMatch> matchesAsPlayerO = new HashSet<>();
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Move> moves = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Friend> friendships = new HashSet<>();
    
    @OneToMany(mappedBy = "friend", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Friend> friendOf = new HashSet<>();
    
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<ChatMessage> sentMessages = new HashSet<>();
}
