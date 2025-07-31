package com.vn.caro_game.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.caro_game.configs.CustomUserDetails;
import com.vn.caro_game.configs.CustomUserDetailsService;
import com.vn.caro_game.constants.FriendConstants;
import com.vn.caro_game.dtos.FriendRequestDto;
import com.vn.caro_game.dtos.FriendResponseDto;
import com.vn.caro_game.dtos.UserSearchResponseDto;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.repositories.UserRepository;
import com.vn.caro_game.repositories.FriendRepository;
import com.vn.caro_game.services.interfaces.IFriendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for FriendController.
 *
 * <p>This test class verifies the controller layer functionality for friend management,
 * including search, request handling, and friend list retrieval operations.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@WebMvcTest(FriendController.class)
@DisplayName("FriendController Tests")
class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IFriendService friendService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FriendRepository friendRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails mockUserDetails;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        mockUserDetails = new CustomUserDetails(testUser);
    }

    @Nested
    @DisplayName("Search Users Tests")
    class SearchUsersTests {

        @Test
        @DisplayName("Should search users successfully")
        @WithMockUser
        void shouldSearchUsersSuccessfully() throws Exception {
            // Given
            FriendRequestDto requestDto = new FriendRequestDto();
            requestDto.setSearchTerm("john");
            
            UserSearchResponseDto user1 = new UserSearchResponseDto();
            user1.setId(2L);
            user1.setUsername("john_doe");
            user1.setDisplayName("John Doe");
            
            UserSearchResponseDto user2 = new UserSearchResponseDto();
            user2.setId(3L);
            user2.setUsername("johnny");
            user2.setDisplayName("Johnny Smith");
            
            List<UserSearchResponseDto> searchResults = Arrays.asList(user1, user2);
            
            when(friendService.searchUsers(eq("john"), eq(1L))).thenReturn(searchResults);

            // When & Then
            mockMvc.perform(post(FriendConstants.FRIEND_BASE_PATH + FriendConstants.SEARCH_PATH)
                    .with(user(mockUserDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].username").value("john_doe"))
                    .andExpect(jsonPath("$.data[1].username").value("johnny"));

            verify(friendService).searchUsers("john", 1L);
        }

        @Test
        @DisplayName("Should return bad request for invalid search request")
        @WithMockUser
        void shouldReturnBadRequestForInvalidSearchRequest() throws Exception {
            // Given
            FriendRequestDto invalidRequest = new FriendRequestDto();
            // searchTerm is null/empty which should be invalid

            // When & Then
            mockMvc.perform(post(FriendConstants.FRIEND_BASE_PATH + FriendConstants.SEARCH_PATH)
                    .with(user(mockUserDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(friendService, never()).searchUsers(any(), any());
        }
    }

    @Nested
    @DisplayName("Friend Request Tests")
    class FriendRequestTests {

        @Test
        @DisplayName("Should send friend request successfully")
        @WithMockUser
        void shouldSendFriendRequestSuccessfully() throws Exception {
            // Given
            Long targetUserId = 2L;
            doNothing().when(friendService).sendFriendRequest(1L, targetUserId);

            // When & Then
            mockMvc.perform(post(FriendConstants.FRIEND_BASE_PATH + FriendConstants.REQUEST_PATH, targetUserId)
                    .with(user(mockUserDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(FriendConstants.FRIEND_REQUEST_SENT_SUCCESS));

            verify(friendService).sendFriendRequest(1L, targetUserId);
        }

        @Test
        @DisplayName("Should accept friend request successfully")
        @WithMockUser
        void shouldAcceptFriendRequestSuccessfully() throws Exception {
            // Given
            Long requestSenderId = 2L;
            doNothing().when(friendService).acceptFriendRequest(1L, requestSenderId);

            // When & Then
            mockMvc.perform(post(FriendConstants.FRIEND_BASE_PATH + FriendConstants.ACCEPT_PATH, requestSenderId)
                    .with(user(mockUserDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(FriendConstants.FRIEND_REQUEST_ACCEPTED_SUCCESS));

            verify(friendService).acceptFriendRequest(1L, requestSenderId);
        }

        @Test
        @DisplayName("Should reject friend request successfully")
        @WithMockUser
        void shouldRejectFriendRequestSuccessfully() throws Exception {
            // Given
            Long requestSenderId = 2L;
            doNothing().when(friendService).rejectFriendRequest(1L, requestSenderId);

            // When & Then
            mockMvc.perform(post(FriendConstants.FRIEND_BASE_PATH + FriendConstants.REJECT_PATH, requestSenderId)
                    .with(user(mockUserDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(FriendConstants.FRIEND_REQUEST_REJECTED_SUCCESS));

            verify(friendService).rejectFriendRequest(1L, requestSenderId);
        }
    }

    @Nested
    @DisplayName("Friend List Tests")
    class FriendListTests {

        @Test
        @DisplayName("Should get friends list successfully")
        @WithMockUser
        void shouldGetFriendsListSuccessfully() throws Exception {
            // Given
            FriendResponseDto friend1 = new FriendResponseDto();
            friend1.setUserId(2L);
            friend1.setUsername("friend1");
            friend1.setDisplayName("Friend One");
            
            FriendResponseDto friend2 = new FriendResponseDto();
            friend2.setUserId(3L);
            friend2.setUsername("friend2");
            friend2.setDisplayName("Friend Two");
            
            List<FriendResponseDto> friendsList = Arrays.asList(friend1, friend2);
            
            when(friendService.getFriendsList(1L)).thenReturn(friendsList);

            // When & Then
            mockMvc.perform(get(FriendConstants.FRIEND_BASE_PATH + FriendConstants.LIST_PATH)
                    .with(user(mockUserDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].username").value("friend1"))
                    .andExpect(jsonPath("$.data[1].username").value("friend2"));

            verify(friendService).getFriendsList(1L);
        }

        @Test
        @DisplayName("Should get pending friend requests successfully")
        @WithMockUser
        void shouldGetPendingFriendRequestsSuccessfully() throws Exception {
            // Given
            FriendResponseDto request1 = new FriendResponseDto();
            request1.setUserId(4L);
            request1.setUsername("requester1");
            request1.setDisplayName("Requester One");
            
            List<FriendResponseDto> pendingRequests = Arrays.asList(request1);
            
            when(friendService.getPendingFriendRequests(1L)).thenReturn(pendingRequests);

            // When & Then
            mockMvc.perform(get(FriendConstants.FRIEND_BASE_PATH + FriendConstants.RECEIVED_REQUESTS_PATH)
                    .with(user(mockUserDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].username").value("requester1"));

            verify(friendService).getPendingFriendRequests(1L);
        }

        @Test
        @DisplayName("Should get sent friend requests successfully")
        @WithMockUser
        void shouldGetSentFriendRequestsSuccessfully() throws Exception {
            // Given
            FriendResponseDto sentRequest = new FriendResponseDto();
            sentRequest.setUserId(5L);
            sentRequest.setUsername("target_user");
            sentRequest.setDisplayName("Target User");
            
            List<FriendResponseDto> sentRequests = Arrays.asList(sentRequest);
            
            when(friendService.getSentFriendRequests(1L)).thenReturn(sentRequests);

            // When & Then
            mockMvc.perform(get(FriendConstants.FRIEND_BASE_PATH + FriendConstants.SENT_REQUESTS_PATH)
                    .with(user(mockUserDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].username").value("target_user"));

            verify(friendService).getSentFriendRequests(1L);
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should require authentication for all endpoints")
        void shouldRequireAuthenticationForAllEndpoints() throws Exception {
            // Search users
            mockMvc.perform(post(FriendConstants.FRIEND_BASE_PATH + FriendConstants.SEARCH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isUnauthorized());

            // Send friend request
            mockMvc.perform(post(FriendConstants.FRIEND_BASE_PATH + FriendConstants.REQUEST_PATH, 2L))
                    .andExpect(status().isUnauthorized());

            // Get friends list
            mockMvc.perform(get(FriendConstants.FRIEND_BASE_PATH + FriendConstants.LIST_PATH))
                    .andExpect(status().isUnauthorized());

            // Get pending requests
            mockMvc.perform(get(FriendConstants.FRIEND_BASE_PATH + FriendConstants.RECEIVED_REQUESTS_PATH))
                    .andExpect(status().isUnauthorized());

            // Get sent requests
            mockMvc.perform(get(FriendConstants.FRIEND_BASE_PATH + FriendConstants.SENT_REQUESTS_PATH))
                    .andExpect(status().isUnauthorized());
        }
    }
}
