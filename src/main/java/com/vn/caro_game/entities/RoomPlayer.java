package com.vn.caro_game.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_players")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomPlayer {
    
    @EmbeddedId
    RoomPlayerId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roomId")
    @JoinColumn(name = "room_id")
    GameRoom room;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    User user;
    
    @Column(name = "is_host", nullable = false)
    Boolean isHost = false;
    
    @CreationTimestamp
    @Column(name = "join_time", nullable = false, updatable = false)
    LocalDateTime joinTime;
    
    @Embeddable
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RoomPlayerId implements java.io.Serializable {
        @Column(name = "room_id")
        Long roomId;
        
        @Column(name = "user_id")
        Long userId;
        
        public RoomPlayerId() {}
        
        public RoomPlayerId(Long roomId, Long userId) {
            this.roomId = roomId;
            this.userId = userId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RoomPlayerId)) return false;
            RoomPlayerId that = (RoomPlayerId) o;
            return roomId.equals(that.roomId) && userId.equals(that.userId);
        }
        
        @Override
        public int hashCode() {
            return java.util.Objects.hash(roomId, userId);
        }
    }
}
