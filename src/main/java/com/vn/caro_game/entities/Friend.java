package com.vn.caro_game.entities;

import com.vn.caro_game.enums.FriendStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "friends")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Friend {
    
    @EmbeddedId
    FriendId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("friendId")
    @JoinColumn(name = "friend_id")
    User friend;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    FriendStatus status;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
    
    @Embeddable
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FriendId implements java.io.Serializable {
        @Column(name = "user_id")
        Long userId;
        
        @Column(name = "friend_id")
        Long friendId;
        
        public FriendId() {}
        
        public FriendId(Long userId, Long friendId) {
            this.userId = userId;
            this.friendId = friendId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FriendId)) return false;
            FriendId friendId1 = (FriendId) o;
            return userId.equals(friendId1.userId) && friendId.equals(friendId1.friendId);
        }
        
        @Override
        public int hashCode() {
            return java.util.Objects.hash(userId, friendId);
        }
    }
}
