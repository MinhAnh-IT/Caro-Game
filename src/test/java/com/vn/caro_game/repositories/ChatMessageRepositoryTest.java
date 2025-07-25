package com.vn.caro_game.repositories;
import com.vn.caro_game.enums.RoomStatus;
import com.vn.caro_game.entities.ChatMessage;
import com.vn.caro_game.entities.GameRoom;
import com.vn.caro_game.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ChatMessageRepository Tests")
class ChatMessageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private User sender1;
    private User sender2;
    private GameRoom testRoom;
    private ChatMessage testMessage;

    @BeforeEach
    void setUp() {
        sender1 = new User();
        sender1.setUsername("sender1");
        sender1.setEmail("sender1@example.com");
        sender1.setPassword("hashedPassword");
        sender1 = entityManager.persistAndFlush(sender1);

        sender2 = new User();
        sender2.setUsername("sender2");
        sender2.setEmail("sender2@example.com");
        sender2.setPassword("hashedPassword");
        sender2 = entityManager.persistAndFlush(sender2);

        testRoom = new GameRoom();
        testRoom.setName("Test Room");
        testRoom.setCreatedBy(sender1);
        testRoom.setStatus(RoomStatus.WAITING);
        testRoom.setIsPrivate(false);
        testRoom = entityManager.persistAndFlush(testRoom);

        testMessage = new ChatMessage();
        testMessage.setRoom(testRoom);
        testMessage.setSender(sender1);
        testMessage.setContent("Hello, World!");
    }

    @Test
    @DisplayName("Should save and retrieve chat message")
    void shouldSaveAndRetrieveChatMessage() {
        // When
        ChatMessage savedMessage = chatMessageRepository.save(testMessage);

        // Then
        assertThat(savedMessage.getId()).isNotNull();
        assertThat(savedMessage.getRoom().getId()).isEqualTo(testRoom.getId());
        assertThat(savedMessage.getSender().getId()).isEqualTo(sender1.getId());
        assertThat(savedMessage.getContent()).isEqualTo("Hello, World!");
        assertThat(savedMessage.getSentAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find messages by room ID ordered by sent time")
    void shouldFindMessagesByRoomIdOrderedBySentTime() {
        // Given
        entityManager.persistAndFlush(testMessage);
        
        // Wait a bit to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ChatMessage secondMessage = new ChatMessage();
        secondMessage.setRoom(testRoom);
        secondMessage.setSender(sender2);
        secondMessage.setContent("Hi there!");
        entityManager.persistAndFlush(secondMessage);
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ChatMessage thirdMessage = new ChatMessage();
        thirdMessage.setRoom(testRoom);
        thirdMessage.setSender(sender1);
        thirdMessage.setContent("How are you?");
        entityManager.persistAndFlush(thirdMessage);

        // When
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderBySentAtAsc(testRoom.getId());

        // Then
        assertThat(messages).hasSize(3);
        assertThat(messages.get(0).getContent()).isEqualTo("Hello, World!");
        assertThat(messages.get(1).getContent()).isEqualTo("Hi there!");
        assertThat(messages.get(2).getContent()).isEqualTo("How are you?");
        
        // Verify chronological order
        assertThat(messages.get(0).getSentAt()).isBefore(messages.get(1).getSentAt());
        assertThat(messages.get(1).getSentAt()).isBefore(messages.get(2).getSentAt());
    }

    @Test
    @DisplayName("Should find messages by room ID and sent after specific time")
    void shouldFindMessagesByRoomIdAndSentAfterSpecificTime() {
        // Given
        LocalDateTime beforeTime = LocalDateTime.now();
        
        entityManager.persistAndFlush(testMessage);
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        LocalDateTime afterFirstMessage = LocalDateTime.now();
        
        ChatMessage secondMessage = new ChatMessage();
        secondMessage.setRoom(testRoom);
        secondMessage.setSender(sender2);
        secondMessage.setContent("Recent message");
        entityManager.persistAndFlush(secondMessage);

        // When
        List<ChatMessage> allMessages = chatMessageRepository.findByRoomIdAndSentAtAfter(
            testRoom.getId(), beforeTime);
        List<ChatMessage> recentMessages = chatMessageRepository.findByRoomIdAndSentAtAfter(
            testRoom.getId(), afterFirstMessage);

        // Then
        assertThat(allMessages).hasSize(2);
        assertThat(recentMessages).hasSize(1);
        assertThat(recentMessages.get(0).getContent()).isEqualTo("Recent message");
    }

    @Test
    @DisplayName("Should find latest messages by room ID with limit")
    void shouldFindLatestMessagesByRoomIdWithLimit() {
        // Given
        for (int i = 1; i <= 5; i++) {
            ChatMessage message = new ChatMessage();
            message.setRoom(testRoom);
            message.setSender(i % 2 == 0 ? sender2 : sender1);
            message.setContent("Message " + i);
            entityManager.persistAndFlush(message);
            
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // When
        List<ChatMessage> latestThree = chatMessageRepository.findLatestMessagesByRoomId(testRoom.getId(), 3);

        // Then
        assertThat(latestThree).hasSize(3);
        // Should be in descending order (latest first)
        assertThat(latestThree.get(0).getContent()).isEqualTo("Message 5");
        assertThat(latestThree.get(1).getContent()).isEqualTo("Message 4");
        assertThat(latestThree.get(2).getContent()).isEqualTo("Message 3");
    }

    @Test
    @DisplayName("Should find messages by sender ID ordered by sent time descending")
    void shouldFindMessagesBySenderIdOrderedBySentTimeDescending() {
        // Given
        entityManager.persistAndFlush(testMessage);
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ChatMessage sender2Message = new ChatMessage();
        sender2Message.setRoom(testRoom);
        sender2Message.setSender(sender2);
        sender2Message.setContent("Message from sender2");
        entityManager.persistAndFlush(sender2Message);
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ChatMessage anotherSender1Message = new ChatMessage();
        anotherSender1Message.setRoom(testRoom);
        anotherSender1Message.setSender(sender1);
        anotherSender1Message.setContent("Another message from sender1");
        entityManager.persistAndFlush(anotherSender1Message);

        // When
        List<ChatMessage> sender1Messages = chatMessageRepository.findBySenderIdOrderBySentAtDesc(sender1.getId());
        List<ChatMessage> sender2Messages = chatMessageRepository.findBySenderIdOrderBySentAtDesc(sender2.getId());

        // Then
        assertThat(sender1Messages).hasSize(2);
        assertThat(sender1Messages.get(0).getContent()).isEqualTo("Another message from sender1"); // Latest first
        assertThat(sender1Messages.get(1).getContent()).isEqualTo("Hello, World!");
        
        assertThat(sender2Messages).hasSize(1);
        assertThat(sender2Messages.get(0).getContent()).isEqualTo("Message from sender2");
    }

    @Test
    @DisplayName("Should count messages by room ID")
    void shouldCountMessagesByRoomId() {
        // Given
        entityManager.persistAndFlush(testMessage);
        
        ChatMessage secondMessage = new ChatMessage();
        secondMessage.setRoom(testRoom);
        secondMessage.setSender(sender2);
        secondMessage.setContent("Second message");
        entityManager.persistAndFlush(secondMessage);
        
        // Create another room with messages
        GameRoom anotherRoom = new GameRoom();
        anotherRoom.setName("Another Room");
        anotherRoom.setCreatedBy(sender2);
        anotherRoom.setStatus(RoomStatus.WAITING);
        anotherRoom.setIsPrivate(false);
        anotherRoom = entityManager.persistAndFlush(anotherRoom);
        
        ChatMessage anotherRoomMessage = new ChatMessage();
        anotherRoomMessage.setRoom(anotherRoom);
        anotherRoomMessage.setSender(sender1);
        anotherRoomMessage.setContent("Message in another room");
        entityManager.persistAndFlush(anotherRoomMessage);

        // When
        long testRoomMessageCount = chatMessageRepository.countMessagesByRoomId(testRoom.getId());
        long anotherRoomMessageCount = chatMessageRepository.countMessagesByRoomId(anotherRoom.getId());

        // Then
        assertThat(testRoomMessageCount).isEqualTo(2);
        assertThat(anotherRoomMessageCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return empty list when room has no messages")
    void shouldReturnEmptyListWhenRoomHasNoMessages() {
        // When
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderBySentAtAsc(testRoom.getId());

        // Then
        assertThat(messages).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when sender has no messages")
    void shouldReturnEmptyListWhenSenderHasNoMessages() {
        // Given
        entityManager.persistAndFlush(testMessage); // Only sender1 has messages

        // When
        List<ChatMessage> sender2Messages = chatMessageRepository.findBySenderIdOrderBySentAtDesc(sender2.getId());

        // Then
        assertThat(sender2Messages).isEmpty();
    }

    @Test
    @DisplayName("Should return zero count when room has no messages")
    void shouldReturnZeroCountWhenRoomHasNoMessages() {
        // When
        long messageCount = chatMessageRepository.countMessagesByRoomId(testRoom.getId());

        // Then
        assertThat(messageCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle empty content messages")
    void shouldHandleEmptyContentMessages() {
        // Given
        testMessage.setContent("");
        
        // When
        ChatMessage savedMessage = chatMessageRepository.save(testMessage);

        // Then
        assertThat(savedMessage.getContent()).isEqualTo("");
        assertThat(savedMessage.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should handle messages across different rooms")
    void shouldHandleMessagesAcrossDifferentRooms() {
        // Given
        entityManager.persistAndFlush(testMessage);
        
        GameRoom anotherRoom = new GameRoom();
        anotherRoom.setName("Another Room");
        anotherRoom.setCreatedBy(sender2);
        anotherRoom.setStatus(RoomStatus.WAITING);
        anotherRoom.setIsPrivate(false);
        anotherRoom = entityManager.persistAndFlush(anotherRoom);
        
        ChatMessage anotherRoomMessage = new ChatMessage();
        anotherRoomMessage.setRoom(anotherRoom);
        anotherRoomMessage.setSender(sender1);
        anotherRoomMessage.setContent("Message in another room");
        entityManager.persistAndFlush(anotherRoomMessage);

        // When
        List<ChatMessage> testRoomMessages = chatMessageRepository.findByRoomIdOrderBySentAtAsc(testRoom.getId());
        List<ChatMessage> anotherRoomMessages = chatMessageRepository.findByRoomIdOrderBySentAtAsc(anotherRoom.getId());

        // Then
        assertThat(testRoomMessages).hasSize(1);
        assertThat(testRoomMessages.get(0).getContent()).isEqualTo("Hello, World!");
        
        assertThat(anotherRoomMessages).hasSize(1);
        assertThat(anotherRoomMessages.get(0).getContent()).isEqualTo("Message in another room");
    }
}
