package com.vn.caro_game.repositories;

import com.vn.caro_game.entities.Friend;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.FriendStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("FriendRepository Tests")
class FriendRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FriendRepository friendRepository;

    private User user1;
    private User user2;
    private User user3;
    private Friend testFriendship;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("hashedPassword");
        user1 = entityManager.persistAndFlush(user1);

        user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("hashedPassword");
        user2 = entityManager.persistAndFlush(user2);

        user3 = new User();
        user3.setUsername("user3");
        user3.setEmail("user3@example.com");
        user3.setPassword("hashedPassword");
        user3 = entityManager.persistAndFlush(user3);

        testFriendship = new Friend();
        Friend.FriendId friendId = new Friend.FriendId();
        friendId.setUserId(user1.getId());
        friendId.setFriendId(user2.getId());
        testFriendship.setId(friendId);
        testFriendship.setUser(user1);
        testFriendship.setFriend(user2);
        testFriendship.setStatus(FriendStatus.ACCEPTED);
    }

    @Test
    @DisplayName("Should save and retrieve friendship")
    void shouldSaveAndRetrieveFriendship() {
        // When
        Friend savedFriendship = entityManager.persistAndFlush(testFriendship);

        // Then
        assertThat(savedFriendship).isNotNull();
        assertThat(savedFriendship.getId()).isNotNull();
        assertThat(savedFriendship.getId().getUserId()).isEqualTo(user1.getId());
        assertThat(savedFriendship.getId().getFriendId()).isEqualTo(user2.getId());
        assertThat(savedFriendship.getUser()).isNotNull();
        assertThat(savedFriendship.getUser().getId()).isEqualTo(user1.getId());
        assertThat(savedFriendship.getFriend()).isNotNull();
        assertThat(savedFriendship.getFriend().getId()).isEqualTo(user2.getId());
        assertThat(savedFriendship.getStatus()).isEqualTo(FriendStatus.ACCEPTED);
        assertThat(savedFriendship.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find friendships by user ID and status")
    void shouldFindFriendshipsByUserIdAndStatus() {
        // Given
        testFriendship.setStatus(FriendStatus.ACCEPTED);
        entityManager.persistAndFlush(testFriendship);
        
        Friend pendingFriendship = new Friend();
        Friend.FriendId pendingId = new Friend.FriendId();
        pendingId.setUserId(user1.getId());
        pendingId.setFriendId(user3.getId());
        pendingFriendship.setId(pendingId);
        pendingFriendship.setUser(user1);
        pendingFriendship.setFriend(user3);
        pendingFriendship.setStatus(FriendStatus.PENDING);
        entityManager.persistAndFlush(pendingFriendship);

        // When
        List<Friend> acceptedFriends = friendRepository.findByUserIdAndStatus(
            user1.getId(), FriendStatus.ACCEPTED);
        List<Friend> pendingFriends = friendRepository.findByUserIdAndStatus(
            user1.getId(), FriendStatus.PENDING);

        // Then
        assertThat(acceptedFriends).hasSize(1);
        assertThat(acceptedFriends.get(0).getFriend().getId()).isEqualTo(user2.getId());
        
        assertThat(pendingFriends).hasSize(1);
        assertThat(pendingFriends.get(0).getFriend().getId()).isEqualTo(user3.getId());
    }

    @Test
    @DisplayName("Should find friendships by friend ID and status")
    void shouldFindFriendshipsByFriendIdAndStatus() {
        // Given
        testFriendship.setStatus(FriendStatus.PENDING);
        entityManager.persistAndFlush(testFriendship);
        
        Friend anotherFriendship = new Friend();
        Friend.FriendId anotherId = new Friend.FriendId();
        anotherId.setUserId(user3.getId());
        anotherId.setFriendId(user2.getId());
        anotherFriendship.setId(anotherId);
        anotherFriendship.setUser(user3);
        anotherFriendship.setFriend(user2);
        anotherFriendship.setStatus(FriendStatus.ACCEPTED);
        entityManager.persistAndFlush(anotherFriendship);

        // When
        List<Friend> pendingRequests = friendRepository.findByFriendIdAndStatus(
            user2.getId(), FriendStatus.PENDING);
        List<Friend> acceptedRequests = friendRepository.findByFriendIdAndStatus(
            user2.getId(), FriendStatus.ACCEPTED);

        // Then
        assertThat(pendingRequests).hasSize(1);
        assertThat(pendingRequests.get(0).getUser().getId()).isEqualTo(user1.getId());
        
        assertThat(acceptedRequests).hasSize(1);
        assertThat(acceptedRequests.get(0).getUser().getId()).isEqualTo(user3.getId());
    }

    @Test
    @DisplayName("Should find accepted friendships by user ID")
    void shouldFindAcceptedFriendshipsByUserId() {
        // Given
        testFriendship.setStatus(FriendStatus.ACCEPTED);
        entityManager.persistAndFlush(testFriendship);
        
        // Create reverse friendship (user2 -> user1)
        Friend reverseFriendship = new Friend();
        Friend.FriendId reverseId = new Friend.FriendId();
        reverseId.setUserId(user2.getId());
        reverseId.setFriendId(user1.getId());
        reverseFriendship.setId(reverseId);
        reverseFriendship.setUser(user2);
        reverseFriendship.setFriend(user1);
        reverseFriendship.setStatus(FriendStatus.ACCEPTED);
        entityManager.persistAndFlush(reverseFriendship);
        
        // Create another friendship where user1 is the friend
        Friend anotherFriendship = new Friend();
        Friend.FriendId anotherId = new Friend.FriendId();
        anotherId.setUserId(user3.getId());
        anotherId.setFriendId(user1.getId());
        anotherFriendship.setId(anotherId);
        anotherFriendship.setUser(user3);
        anotherFriendship.setFriend(user1);
        anotherFriendship.setStatus(FriendStatus.ACCEPTED);
        entityManager.persistAndFlush(anotherFriendship);

        // When
        List<Friend> user1Friendships = friendRepository.findAcceptedFriendshipsByUserId(user1.getId());

        // Then - Should find all friendships where user1 is either the user or the friend
        assertThat(user1Friendships).hasSize(3);
        assertThat(user1Friendships).extracting(f -> f.getUser().getId() + "-" + f.getFriend().getId())
                                  .containsExactlyInAnyOrder(
                                      user1.getId() + "-" + user2.getId(),
                                      user2.getId() + "-" + user1.getId(),
                                      user3.getId() + "-" + user1.getId());
    }

    @Test
    @DisplayName("Should find pending friend requests for user")
    void shouldFindPendingFriendRequestsForUser() {
        // Given - Create friend requests sent TO user2
        testFriendship.setStatus(FriendStatus.PENDING);
        testFriendship.setUser(user1);
        testFriendship.setFriend(user2); // user1 -> user2 (PENDING)
        entityManager.persistAndFlush(testFriendship);
        
        Friend anotherRequest = new Friend();
        Friend.FriendId anotherId = new Friend.FriendId();
        anotherId.setUserId(user3.getId());
        anotherId.setFriendId(user2.getId());
        anotherRequest.setId(anotherId);
        anotherRequest.setUser(user3);
        anotherRequest.setFriend(user2); // user3 -> user2 (PENDING)
        anotherRequest.setStatus(FriendStatus.PENDING);
        entityManager.persistAndFlush(anotherRequest);

        // When
        List<Friend> pendingRequests = friendRepository.findPendingFriendRequestsForUser(user2.getId());

        // Then
        assertThat(pendingRequests).hasSize(2);
        assertThat(pendingRequests).extracting(f -> f.getUser().getId())
                                 .containsExactlyInAnyOrder(user1.getId(), user3.getId());
    }

    @Test
    @DisplayName("Should check if friendship exists")
    void shouldCheckIfFriendshipExists() {
        // Given
        entityManager.persistAndFlush(testFriendship);

        // When & Then
        assertThat(friendRepository.existsByUserIdAndFriendId(user1.getId(), user2.getId())).isTrue();
        assertThat(friendRepository.existsByUserIdAndFriendId(user2.getId(), user1.getId())).isFalse();
        assertThat(friendRepository.existsByUserIdAndFriendId(user1.getId(), user3.getId())).isFalse();
    }

    @Test
    @DisplayName("Should handle different friendship statuses")
    void shouldHandleDifferentFriendshipStatuses() {
        // Given
        testFriendship.setStatus(FriendStatus.BLOCKED);
        entityManager.persistAndFlush(testFriendship);

        // When
        List<Friend> blockedFriends = friendRepository.findByUserIdAndStatus(
            user1.getId(), FriendStatus.BLOCKED);
        List<Friend> acceptedFriends = friendRepository.findByUserIdAndStatus(
            user1.getId(), FriendStatus.ACCEPTED);

        // Then
        assertThat(blockedFriends).hasSize(1);
        assertThat(blockedFriends.get(0).getStatus()).isEqualTo(FriendStatus.BLOCKED);
        
        assertThat(acceptedFriends).isEmpty();
    }

    @Test
    @DisplayName("Should return empty lists when no friendships exist")
    void shouldReturnEmptyListsWhenNoFriendshipsExist() {
        // When
        List<Friend> userFriends = friendRepository.findByUserIdAndStatus(
            user1.getId(), FriendStatus.ACCEPTED);
        List<Friend> pendingRequests = friendRepository.findPendingFriendRequestsForUser(user1.getId());
        List<Friend> acceptedFriendships = friendRepository.findAcceptedFriendshipsByUserId(user1.getId());

        // Then
        assertThat(userFriends).isEmpty();
        assertThat(pendingRequests).isEmpty();
        assertThat(acceptedFriendships).isEmpty();
    }

    @Test
    @DisplayName("Should handle composite key correctly")
    void shouldHandleCompositeKeyCorrectly() {
        // Given
        entityManager.persistAndFlush(testFriendship);
        
        // Create another friendship with same users but reversed
        Friend reverseFriendship = new Friend();
        Friend.FriendId reverseId = new Friend.FriendId();
        reverseId.setUserId(user2.getId());
        reverseId.setFriendId(user1.getId());
        reverseFriendship.setId(reverseId);
        reverseFriendship.setUser(user2);
        reverseFriendship.setFriend(user1);
        reverseFriendship.setStatus(FriendStatus.PENDING);
        entityManager.persistAndFlush(reverseFriendship);

        // When
        List<Friend> user1Friends = friendRepository.findByUserIdAndStatus(
            user1.getId(), FriendStatus.ACCEPTED);
        List<Friend> user2Friends = friendRepository.findByUserIdAndStatus(
            user2.getId(), FriendStatus.PENDING);

        // Then
        assertThat(user1Friends).hasSize(1);
        assertThat(user1Friends.get(0).getFriend().getId()).isEqualTo(user2.getId());
        
        assertThat(user2Friends).hasSize(1);
        assertThat(user2Friends.get(0).getFriend().getId()).isEqualTo(user1.getId());
    }
}
