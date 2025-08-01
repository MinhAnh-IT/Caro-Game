package com.vn.caro_game.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.caro_game.configs.CustomUserDetails;
import com.vn.caro_game.dtos.request.CreateRoomRequest;
import com.vn.caro_game.dtos.request.InviteFriendRequest;
import com.vn.caro_game.dtos.response.GameRoomResponse;
import com.vn.caro_game.dtos.response.PublicRoomResponse;
import com.vn.caro_game.dtos.response.UserSummaryResponse;
import com.vn.caro_game.entities.User;
import com.vn.caro_game.enums.RoomStatus;
import com.vn.caro_game.repositories.UserRepository;
import com.vn.caro_game.services.interfaces.GameRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GameRoomController.class)
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class GameRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameRoomService gameRoomService;

    @MockBean
    private UserRepository userRepository;

    // Additional MockBeans needed for dependencies
    @MockBean
    private com.vn.caro_game.repositories.GameRoomRepository gameRoomRepository;

    @MockBean
    private com.vn.caro_game.repositories.RoomPlayerRepository roomPlayerRepository;

    @MockBean
    private com.vn.caro_game.repositories.ChatMessageRepository chatMessageRepository;

    @MockBean
    private com.vn.caro_game.repositories.FriendRepository friendRepository;

    @MockBean
    private com.vn.caro_game.integrations.redis.RedisService redisService;

    @MockBean
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @MockBean
    private com.vn.caro_game.mappers.GameRoomMapper gameRoomMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private GameRoomResponse gameRoomResponse;
    private PublicRoomResponse publicRoomResponse;
    private CustomUserDetails mockUserDetails;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        mockUserDetails = new CustomUserDetails(testUser);
        
        // Create GameRoomResponse
        UserSummaryResponse creator = new UserSummaryResponse(1L, "Test User", null, "testuser");
        gameRoomResponse = new GameRoomResponse(
                1L, 
                "Test Room", 
                RoomStatus.WAITING, 
                false, 
                null, 
                creator, 
                LocalDateTime.now(), 
                new ArrayList<>(), 
                0, 
                2
        );

        // Create PublicRoomResponse
        publicRoomResponse = new PublicRoomResponse(
                1L,
                "Test Room",
                RoomStatus.WAITING,
                "Test User",
                LocalDateTime.now(),
                0,
                2,
                true
        );
    }

    @Test
    @WithMockUser
    void createRoom_WithValidRequest_ShouldReturnCreatedRoom() throws Exception {
        // Given
        CreateRoomRequest request = new CreateRoomRequest();
        request.setName("Test Room");
        request.setIsPrivate(false);

        when(gameRoomService.createRoom(any(CreateRoomRequest.class), anyLong()))
                .thenReturn(gameRoomResponse);

        // When & Then
        mockMvc.perform(post("/api/rooms")
                .with(csrf())
                .with(user(mockUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Room created successfully"));
    }

    @Test
    @WithMockUser
    void quickPlay_ShouldReturnRoomForQuickPlay() throws Exception {
        // Given
        when(gameRoomService.findOrCreatePublicRoom(anyLong()))
                .thenReturn(gameRoomResponse);

        // When & Then
        mockMvc.perform(post("/api/rooms/quick-play")
                .with(csrf())
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Room ready for quick play"));
    }

    @Test
    @WithMockUser
    void getRoomDetails_WithValidRoomId_ShouldReturnRoomDetails() throws Exception {
        // Given
        Long roomId = 1L;
        when(gameRoomService.getRoomDetails(anyLong(), anyLong()))
                .thenReturn(gameRoomResponse);

        // When & Then
        mockMvc.perform(get("/api/rooms/{roomId}", roomId)
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Room details retrieved successfully"));
    }

    @Test
    @WithMockUser
    void getPublicRooms_ShouldReturnPagedRooms() throws Exception {
        // Given
        Page<PublicRoomResponse> pagedResponse = new PageImpl<>(List.of(publicRoomResponse), PageRequest.of(0, 20), 1);
        when(gameRoomService.getPublicRooms(any(PageRequest.class))).thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/rooms/public")
                .with(user(mockUserDetails))
                .param("page", "0")
                .param("size", "20")
                .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Public rooms retrieved successfully"));
    }

    @Test
    @WithMockUser
    void getUserRooms_ShouldReturnUserRooms() throws Exception {
        // Given
        Page<GameRoomResponse> pagedResponse = new PageImpl<>(List.of(gameRoomResponse), PageRequest.of(0, 10), 1);
        when(gameRoomService.getUserRooms(anyLong(), any(PageRequest.class))).thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/rooms/user-rooms")
                .with(user(mockUserDetails))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User rooms retrieved successfully"));
    }

    @Test
    @WithMockUser
    void getCurrentRoom_ShouldReturnCurrentRoom() throws Exception {
        // Given
        when(gameRoomService.getCurrentUserRoom(anyLong()))
                .thenReturn(gameRoomResponse);

        // When & Then
        mockMvc.perform(get("/api/rooms/current")
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Current room retrieved successfully"));
    }

    @Test
    @WithMockUser
    void inviteFriend_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        Long roomId = 1L;
        InviteFriendRequest request = new InviteFriendRequest();
        request.setFriendUserId(2L);

        // When & Then
        mockMvc.perform(post("/api/rooms/{roomId}/invite", roomId)
                .with(csrf())
                .with(user(mockUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Invitation sent successfully"));
    }
}
