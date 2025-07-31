package com.vn.caro_game.services.impl;

import com.vn.caro_game.constants.FriendConstants;
import com.vn.caro_game.dtos.FriendResponseDto;
import com.vn.caro_game.dtos.UserSearchResponseDto;
import com.vn.caro_game.entities.Friend;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.FriendStatus;
import com.vn.caro_game.enums.StatusCode;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.repositories.FriendRepository;
import com.vn.caro_game.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FriendServiceImpl.
 *
 * <p>This test class verifies the friend management business logic including
 * user search, friend requests, and relationship management following
 * clean architecture principles.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FriendServiceImpl Tests")
class FriendServiceImplTest {

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FriendServiceImpl friendService;

    private User testUser1;
    private User testUser2;
    private Friend testFriendRequest;

    @BeforeEach
    void setUp() {
        testUser1 = new User();
        testUser1.setId(1L);
        testUser1.setUsername("user1");
        testUser1.setDisplayName("User One");
        testUser1.setEmail("user1@example.com");
        testUser1.setAvatarUrl("/avatar1.jpg");

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setUsername("user2");
        testUser2.setDisplayName("User Two");
        testUser2.setEmail("user2@example.com");
        testUser2.setAvatarUrl("/avatar2.jpg");

        testFriendRequest = new Friend();
        testFriendRequest.setId(new Friend.FriendId(1L, 2L));
        testFriendRequest.setStatus(FriendStatus.PENDING);
        testFriendRequest.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Search Users Tests")
    class SearchUsersTests {

        @Test
        @DisplayName("Should return users matching search term")
        void shouldReturnUsersMatchingSearchTerm() {
            // Given
            String searchTerm = "User";
            Long currentUserId = 1L;
            List<User> mockUsers = Arrays.asList(testUser2);
            
            when(userRepository.findUsersByDisplayNameOrUsername(searchTerm, currentUserId))
                    .thenReturn(mockUsers);
            when(friendRepository.existsFriendshipWithStatuses(eq(currentUserId), eq(2L), any()))
                    .thenReturn(false);

            // When
            List<UserSearchResponseDto> result = friendService.searchUsers(searchTerm, currentUserId);

            // Then
            assertThat(result).hasSize(1);
            UserSearchResponseDto dto = result.get(0);
            assertThat(dto.getId()).isEqualTo(2L);
            assertThat(dto.getUsername()).isEqualTo("user2");
            assertThat(dto.getDisplayName()).isEqualTo("User Two");
            assertThat(dto.getRelationshipStatus()).isEqualTo(FriendConstants.RELATIONSHIP_STATUS_NONE);
            assertThat(dto.getCanSendRequest()).isTrue();

            verify(userRepository).findUsersByDisplayNameOrUsername(searchTerm, currentUserId);
        }

        @Test
        @DisplayName("Should return empty list when no users match")
        void shouldReturnEmptyListWhenNoUsersMatch() {
            // Given
            String searchTerm = "NonExistent";
            Long currentUserId = 1L;
            
            when(userRepository.findUsersByDisplayNameOrUsername(searchTerm, currentUserId))
                    .thenReturn(Arrays.asList());

            // When
            List<UserSearchResponseDto> result = friendService.searchUsers(searchTerm, currentUserId);

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).findUsersByDisplayNameOrUsername(searchTerm, currentUserId);
        }

        @Test
        @DisplayName("Should show correct relationship status for existing friends")
        void shouldShowCorrectRelationshipStatusForExistingFriends() {
            // Given
            String searchTerm = "User";
            Long currentUserId = 1L;
            List<User> mockUsers = Arrays.asList(testUser2);
            
            when(userRepository.findUsersByDisplayNameOrUsername(searchTerm, currentUserId))
                    .thenReturn(mockUsers);
            when(friendRepository.existsFriendshipWithStatuses(eq(currentUserId), eq(2L), 
                    eq(List.of(FriendStatus.ACCEPTED)))).thenReturn(true);

            // When
            List<UserSearchResponseDto> result = friendService.searchUsers(searchTerm, currentUserId);

            // Then
            assertThat(result).hasSize(1);
            UserSearchResponseDto dto = result.get(0);
            assertThat(dto.getRelationshipStatus()).isEqualTo(FriendConstants.RELATIONSHIP_STATUS_FRIENDS);
            assertThat(dto.getCanSendRequest()).isFalse();
        }
    }

    @Nested
    @DisplayName("Send Friend Request Tests")
    class SendFriendRequestTests {

        @Test
        @DisplayName("Should send friend request successfully")
        void shouldSendFriendRequestSuccessfully() {
            // Given
            Long fromUserId = 1L;
            Long toUserId = 2L;
            
            when(userRepository.findById(toUserId)).thenReturn(Optional.of(testUser2));
            when(friendRepository.existsFriendshipWithStatuses(fromUserId, toUserId,
                    Arrays.asList(FriendStatus.PENDING, FriendStatus.ACCEPTED))).thenReturn(false);

            // When
            friendService.sendFriendRequest(fromUserId, toUserId);

            // Then
            verify(userRepository).findById(toUserId);
            verify(friendRepository).existsFriendshipWithStatuses(fromUserId, toUserId,
                    Arrays.asList(FriendStatus.PENDING, FriendStatus.ACCEPTED));
            verify(friendRepository).save(any(Friend.class));
        }

        @Test
        @DisplayName("Should throw exception when trying to add yourself")
        void shouldThrowExceptionWhenTryingToAddYourself() {
            // Given
            Long userId = 1L;

            // When & Then
            assertThatThrownBy(() -> friendService.sendFriendRequest(userId, userId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("statusCode", StatusCode.CANNOT_ADD_YOURSELF);

            verify(friendRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when target user not found")
        void shouldThrowExceptionWhenTargetUserNotFound() {
            // Given
            Long fromUserId = 1L;
            Long toUserId = 999L;
            
            when(userRepository.findById(toUserId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> friendService.sendFriendRequest(fromUserId, toUserId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("statusCode", StatusCode.USER_NOT_FOUND);

            verify(friendRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when relationship already exists")
        void shouldThrowExceptionWhenRelationshipAlreadyExists() {
            // Given
            Long fromUserId = 1L;
            Long toUserId = 2L;
            
            when(userRepository.findById(toUserId)).thenReturn(Optional.of(testUser2));
            when(friendRepository.existsFriendshipWithStatuses(fromUserId, toUserId,
                    Arrays.asList(FriendStatus.PENDING, FriendStatus.ACCEPTED))).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> friendService.sendFriendRequest(fromUserId, toUserId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("statusCode", StatusCode.FRIEND_REQUEST_ALREADY_SENT);

            verify(friendRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Accept Friend Request Tests")
    class AcceptFriendRequestTests {

        @Test
        @DisplayName("Should accept friend request successfully")
        void shouldAcceptFriendRequestSuccessfully() {
            // Given
            Long userId = 2L;
            Long fromUserId = 1L;
            
            when(friendRepository.findByUserIdAndFriendId(fromUserId, userId))
                    .thenReturn(Optional.of(testFriendRequest));

            // When
            friendService.acceptFriendRequest(userId, fromUserId);

            // Then
            verify(friendRepository).findByUserIdAndFriendId(fromUserId, userId);
            verify(friendRepository, times(2)).save(any(Friend.class)); // Original + reverse friendship
        }

        @Test
        @DisplayName("Should throw exception when friend request not found")
        void shouldThrowExceptionWhenFriendRequestNotFound() {
            // Given
            Long userId = 2L;
            Long fromUserId = 1L;
            
            when(friendRepository.findByUserIdAndFriendId(fromUserId, userId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> friendService.acceptFriendRequest(userId, fromUserId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("statusCode", StatusCode.FRIEND_REQUEST_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when request already responded")
        void shouldThrowExceptionWhenRequestAlreadyResponded() {
            // Given
            Long userId = 2L;
            Long fromUserId = 1L;
            testFriendRequest.setStatus(FriendStatus.ACCEPTED);
            
            when(friendRepository.findByUserIdAndFriendId(fromUserId, userId))
                    .thenReturn(Optional.of(testFriendRequest));

            // When & Then
            assertThatThrownBy(() -> friendService.acceptFriendRequest(userId, fromUserId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("statusCode", StatusCode.FRIEND_REQUEST_ALREADY_RESPONDED);
        }
    }

    @Nested
    @DisplayName("Reject Friend Request Tests")
    class RejectFriendRequestTests {

        @Test
        @DisplayName("Should reject friend request successfully")
        void shouldRejectFriendRequestSuccessfully() {
            // Given
            Long userId = 2L;
            Long fromUserId = 1L;
            
            when(friendRepository.findByUserIdAndFriendId(fromUserId, userId))
                    .thenReturn(Optional.of(testFriendRequest));

            // When
            friendService.rejectFriendRequest(userId, fromUserId);

            // Then
            verify(friendRepository).findByUserIdAndFriendId(fromUserId, userId);
            verify(friendRepository).delete(testFriendRequest);
        }

        @Test
        @DisplayName("Should throw exception when friend request not found for rejection")
        void shouldThrowExceptionWhenFriendRequestNotFoundForRejection() {
            // Given
            Long userId = 2L;
            Long fromUserId = 1L;
            
            when(friendRepository.findByUserIdAndFriendId(fromUserId, userId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> friendService.rejectFriendRequest(userId, fromUserId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("statusCode", StatusCode.FRIEND_REQUEST_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Get Friends List Tests")
    class GetFriendsListTests {

        @Test
        @DisplayName("Should return friends list successfully")
        void shouldReturnFriendsListSuccessfully() {
            // Given
            Long userId = 1L;
            Friend friendship = new Friend();
            friendship.setId(new Friend.FriendId(1L, 2L));
            friendship.setStatus(FriendStatus.ACCEPTED);
            friendship.setCreatedAt(LocalDateTime.now());
            
            when(friendRepository.findAllFriendsWithStatus(userId, FriendStatus.ACCEPTED))
                    .thenReturn(Arrays.asList(friendship));
            when(userRepository.findById(2L)).thenReturn(Optional.of(testUser2));

            // When
            List<FriendResponseDto> result = friendService.getFriendsList(userId);

            // Then
            assertThat(result).hasSize(1);
            FriendResponseDto dto = result.get(0);
            assertThat(dto.getUserId()).isEqualTo(2L);
            assertThat(dto.getUsername()).isEqualTo("user2");
            assertThat(dto.getDisplayName()).isEqualTo("User Two");
            assertThat(dto.getStatus()).isEqualTo(FriendStatus.ACCEPTED);
        }

        @Test
        @DisplayName("Should return empty list when no friends")
        void shouldReturnEmptyListWhenNoFriends() {
            // Given
            Long userId = 1L;
            
            when(friendRepository.findAllFriendsWithStatus(userId, FriendStatus.ACCEPTED))
                    .thenReturn(Arrays.asList());

            // When
            List<FriendResponseDto> result = friendService.getFriendsList(userId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Pending Friend Requests Tests")
    class GetPendingFriendRequestsTests {

        @Test
        @DisplayName("Should return pending friend requests successfully")
        void shouldReturnPendingFriendRequestsSuccessfully() {
            // Given
            Long userId = 2L;
            
            when(friendRepository.findByFriendIdAndStatus(userId, FriendStatus.PENDING))
                    .thenReturn(Arrays.asList(testFriendRequest));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser1));

            // When
            List<FriendResponseDto> result = friendService.getPendingFriendRequests(userId);

            // Then
            assertThat(result).hasSize(1);
            FriendResponseDto dto = result.get(0);
            assertThat(dto.getUserId()).isEqualTo(1L);
            assertThat(dto.getUsername()).isEqualTo("user1");
            assertThat(dto.getStatus()).isEqualTo(FriendStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Get Sent Friend Requests Tests")
    class GetSentFriendRequestsTests {

        @Test
        @DisplayName("Should return sent friend requests successfully")
        void shouldReturnSentFriendRequestsSuccessfully() {
            // Given
            Long userId = 1L;
            
            when(friendRepository.findByUserIdAndStatus(userId, FriendStatus.PENDING))
                    .thenReturn(Arrays.asList(testFriendRequest));
            when(userRepository.findById(2L)).thenReturn(Optional.of(testUser2));

            // When
            List<FriendResponseDto> result = friendService.getSentFriendRequests(userId);

            // Then
            assertThat(result).hasSize(1);
            FriendResponseDto dto = result.get(0);
            assertThat(dto.getUserId()).isEqualTo(2L);
            assertThat(dto.getUsername()).isEqualTo("user2");
            assertThat(dto.getStatus()).isEqualTo(FriendStatus.PENDING);
        }
    }
}
