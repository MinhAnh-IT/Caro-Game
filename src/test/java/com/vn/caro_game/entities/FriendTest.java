package com.vn.caro_game.entities;

import com.vn.caro_game.enums.FriendStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Friend Entity Tests")
class FriendTest {

    private User user1;
    private User user2;
    private Friend friendship;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("hashedPassword");

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("hashedPassword");

        friendship = new Friend();
        Friend.FriendId friendId = new Friend.FriendId();
        friendId.setUserId(user1.getId());
        friendId.setFriendId(user2.getId());
        friendship.setId(friendId);
        friendship.setUser(user1);
        friendship.setFriend(user2);
        friendship.setStatus(FriendStatus.ACCEPTED);
        friendship.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create Friend with all required fields")
    void shouldCreateFriendWithAllRequiredFields() {
        assertThat(friendship).isNotNull();
        assertThat(friendship.getId()).isNotNull();
        assertThat(friendship.getId().getUserId()).isEqualTo(1L);
        assertThat(friendship.getId().getFriendId()).isEqualTo(2L);
        assertThat(friendship.getUser()).isEqualTo(user1);
        assertThat(friendship.getFriend()).isEqualTo(user2);
        assertThat(friendship.getStatus()).isEqualTo(FriendStatus.ACCEPTED);
        assertThat(friendship.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle composite key properly")
    void shouldHandleCompositeKeyProperly() {
        Friend.FriendId compositeId = friendship.getId();
        
        assertThat(compositeId).isNotNull();
        assertThat(compositeId.getUserId()).isEqualTo(user1.getId());
        assertThat(compositeId.getFriendId()).isEqualTo(user2.getId());
    }

    @Test
    @DisplayName("Should handle all friendship statuses")
    void shouldHandleAllFriendshipStatuses() {
        // Test PENDING status
        friendship.setStatus(FriendStatus.PENDING);
        assertThat(friendship.getStatus()).isEqualTo(FriendStatus.PENDING);

        // Test ACCEPTED status
        friendship.setStatus(FriendStatus.ACCEPTED);
        assertThat(friendship.getStatus()).isEqualTo(FriendStatus.ACCEPTED);

        // Test BLOCKED status
        friendship.setStatus(FriendStatus.BLOCKED);
        assertThat(friendship.getStatus()).isEqualTo(FriendStatus.BLOCKED);
    }

    @Test
    @DisplayName("Should validate FriendStatus enum values")
    void shouldValidateFriendStatusEnumValues() {
        FriendStatus[] statuses = FriendStatus.values();
        
        assertThat(statuses).hasSize(3);
        assertThat(statuses).contains(
            FriendStatus.PENDING,
            FriendStatus.ACCEPTED,
            FriendStatus.BLOCKED
        );
    }

    @Test
    @DisplayName("Should maintain bidirectional relationship")
    void shouldMaintainBidirectionalRelationship() {
        assertThat(friendship.getUser()).isEqualTo(user1);
        assertThat(friendship.getFriend()).isEqualTo(user2);
        
        // Verify the relationship is directional
        assertThat(friendship.getUser().getId()).isEqualTo(friendship.getId().getUserId());
        assertThat(friendship.getFriend().getId()).isEqualTo(friendship.getId().getFriendId());
    }

    @Test
    @DisplayName("Should handle creation timestamp")
    void shouldHandleCreationTimestamp() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        LocalDateTime timestamp = LocalDateTime.now();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        friendship.setCreatedAt(timestamp);
        
        assertThat(friendship.getCreatedAt()).isAfter(before);
        assertThat(friendship.getCreatedAt()).isBefore(after);
        assertThat(friendship.getCreatedAt()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("Should handle null values appropriately")
    void shouldHandleNullValuesAppropriately() {
        Friend newFriendship = new Friend();
        
        assertThat(newFriendship.getId()).isNull();
        assertThat(newFriendship.getUser()).isNull();
        assertThat(newFriendship.getFriend()).isNull();
        assertThat(newFriendship.getStatus()).isNull();
        assertThat(newFriendship.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Should validate composite key equality")
    void shouldValidateCompositeKeyEquality() {
        Friend.FriendId id1 = new Friend.FriendId();
        id1.setUserId(1L);
        id1.setFriendId(2L);
        
        Friend.FriendId id2 = new Friend.FriendId();
        id2.setUserId(1L);
        id2.setFriendId(2L);
        
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("Should validate composite key inequality")
    void shouldValidateCompositeKeyInequality() {
        Friend.FriendId id1 = new Friend.FriendId();
        id1.setUserId(1L);
        id1.setFriendId(2L);
        
        Friend.FriendId id2 = new Friend.FriendId();
        id2.setUserId(2L);
        id2.setFriendId(1L);
        
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("Should handle friendship status transitions")
    void shouldHandleFriendshipStatusTransitions() {
        // Start with PENDING
        friendship.setStatus(FriendStatus.PENDING);
        assertThat(friendship.getStatus()).isEqualTo(FriendStatus.PENDING);
        
        // Accept friendship
        friendship.setStatus(FriendStatus.ACCEPTED);
        assertThat(friendship.getStatus()).isEqualTo(FriendStatus.ACCEPTED);
        
        // Block friendship
        friendship.setStatus(FriendStatus.BLOCKED);
        assertThat(friendship.getStatus()).isEqualTo(FriendStatus.BLOCKED);
    }

    @Test
    @DisplayName("Should maintain user relationships correctly")
    void shouldMaintainUserRelationshipsCorrectly() {
        assertThat(friendship.getUser()).isNotEqualTo(friendship.getFriend());
        assertThat(friendship.getUser().getId()).isNotEqualTo(friendship.getFriend().getId());
        assertThat(friendship.getUser().getUsername()).isNotEqualTo(friendship.getFriend().getUsername());
        assertThat(friendship.getUser().getEmail()).isNotEqualTo(friendship.getFriend().getEmail());
    }

    @Test
    @DisplayName("Should support reversible relationships")
    void shouldSupportReversibleRelationships() {
        // Create reverse friendship
        Friend reverseFriendship = new Friend();
        Friend.FriendId reverseId = new Friend.FriendId();
        reverseId.setUserId(user2.getId());
        reverseId.setFriendId(user1.getId());
        reverseFriendship.setId(reverseId);
        reverseFriendship.setUser(user2);
        reverseFriendship.setFriend(user1);
        reverseFriendship.setStatus(FriendStatus.ACCEPTED);
        
        // Verify both directions
        assertThat(friendship.getUser()).isEqualTo(reverseFriendship.getFriend());
        assertThat(friendship.getFriend()).isEqualTo(reverseFriendship.getUser());
        assertThat(friendship.getId().getUserId()).isEqualTo(reverseFriendship.getId().getFriendId());
        assertThat(friendship.getId().getFriendId()).isEqualTo(reverseFriendship.getId().getUserId());
    }

    @Test
    @DisplayName("Should handle different users correctly")
    void shouldHandleDifferentUsersCorrectly() {
        User user3 = new User();
        user3.setId(3L);
        user3.setUsername("user3");
        user3.setEmail("user3@example.com");
        
        friendship.setFriend(user3);
        Friend.FriendId newId = new Friend.FriendId();
        newId.setUserId(user1.getId());
        newId.setFriendId(user3.getId());
        friendship.setId(newId);
        
        assertThat(friendship.getFriend()).isEqualTo(user3);
        assertThat(friendship.getId().getFriendId()).isEqualTo(user3.getId());
    }

    @Test
    @DisplayName("Should validate friendship constraints")
    void shouldValidateFriendshipConstraints() {
        // A user shouldn't be friends with themselves
        Friend.FriendId selfId = new Friend.FriendId();
        selfId.setUserId(1L);
        selfId.setFriendId(1L);
        
        // This would be a constraint violation in real application
        assertThat(selfId.getUserId()).isEqualTo(selfId.getFriendId());
    }

    @Test
    @DisplayName("Should handle status-based filtering")
    void shouldHandleStatusBasedFiltering() {
        friendship.setStatus(FriendStatus.PENDING);
        assertThat(friendship.getStatus()).isEqualTo(FriendStatus.PENDING);
        assertThat(friendship.getStatus()).isNotEqualTo(FriendStatus.ACCEPTED);
        assertThat(friendship.getStatus()).isNotEqualTo(FriendStatus.BLOCKED);
    }
}
