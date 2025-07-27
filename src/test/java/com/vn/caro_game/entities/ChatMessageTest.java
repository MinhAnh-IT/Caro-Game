package com.vn.caro_game.entities;

import com.vn.caro_game.enums.RoomStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatMessage Entity Tests")
class ChatMessageTest {

    private User sender;
    private GameRoom room;
    private ChatMessage chatMessage;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");
        sender.setEmail("sender@example.com");
        sender.setPassword("hashedPassword");

        room = new GameRoom();
        room.setId(1L);
        room.setName("Test Room");
        room.setCreatedBy(sender);
        room.setStatus(RoomStatus.WAITING);
        room.setIsPrivate(false);

        chatMessage = new ChatMessage();
        chatMessage.setId(1L);
        chatMessage.setRoom(room);
        chatMessage.setSender(sender);
        chatMessage.setContent("Hello, this is a test message!");
        chatMessage.setSentAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create ChatMessage with all required fields")
    void shouldCreateChatMessageWithAllRequiredFields() {
        assertThat(chatMessage).isNotNull();
        assertThat(chatMessage.getId()).isEqualTo(1L);
        assertThat(chatMessage.getRoom()).isEqualTo(room);
        assertThat(chatMessage.getSender()).isEqualTo(sender);
        assertThat(chatMessage.getContent()).isEqualTo("Hello, this is a test message!");
        assertThat(chatMessage.getSentAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle message content correctly")
    void shouldHandleMessageContentCorrectly() {
        // Test short message
        chatMessage.setContent("Hi!");
        assertThat(chatMessage.getContent()).isEqualTo("Hi!");

        // Test long message
        String longMessage = "This is a very long message that contains a lot of text to test the content handling capabilities of the chat message entity.";
        chatMessage.setContent(longMessage);
        assertThat(chatMessage.getContent()).isEqualTo(longMessage);

        // Test empty message
        chatMessage.setContent("");
        assertThat(chatMessage.getContent()).isEqualTo("");

        // Test special characters
        chatMessage.setContent("Special chars: !@#$%^&*()");
        assertThat(chatMessage.getContent()).isEqualTo("Special chars: !@#$%^&*()");
    }

    @Test
    @DisplayName("Should maintain relationship with GameRoom")
    void shouldMaintainRelationshipWithGameRoom() {
        assertThat(chatMessage.getRoom()).isEqualTo(room);
        
        GameRoom newRoom = new GameRoom();
        newRoom.setId(2L);
        newRoom.setName("New Room");
        newRoom.setCreatedBy(sender);
        
        chatMessage.setRoom(newRoom);
        assertThat(chatMessage.getRoom()).isEqualTo(newRoom);
        assertThat(chatMessage.getRoom().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should maintain relationship with sender User")
    void shouldMaintainRelationshipWithSenderUser() {
        assertThat(chatMessage.getSender()).isEqualTo(sender);
        
        User newSender = new User();
        newSender.setId(2L);
        newSender.setUsername("newsender");
        newSender.setEmail("newsender@example.com");
        
        chatMessage.setSender(newSender);
        assertThat(chatMessage.getSender()).isEqualTo(newSender);
        assertThat(chatMessage.getSender().getUsername()).isEqualTo("newsender");
    }

    @Test
    @DisplayName("Should handle timestamp correctly")
    void shouldHandleTimestampCorrectly() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        LocalDateTime timestamp = LocalDateTime.now();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        chatMessage.setSentAt(timestamp);
        
        assertThat(chatMessage.getSentAt()).isAfter(before);
        assertThat(chatMessage.getSentAt()).isBefore(after);
        assertThat(chatMessage.getSentAt()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("Should handle null values appropriately")
    void shouldHandleNullValuesAppropriately() {
        ChatMessage newMessage = new ChatMessage();
        
        assertThat(newMessage.getId()).isNull();
        assertThat(newMessage.getRoom()).isNull();
        assertThat(newMessage.getSender()).isNull();
        assertThat(newMessage.getContent()).isNull();
        assertThat(newMessage.getSentAt()).isNull();
    }

    @Test
    @DisplayName("Should support message ordering by timestamp")
    void shouldSupportMessageOrderingByTimestamp() {
        ChatMessage message1 = new ChatMessage();
        message1.setSentAt(LocalDateTime.now().minusMinutes(2));

        ChatMessage message2 = new ChatMessage();
        message2.setSentAt(LocalDateTime.now().minusMinutes(1));

        ChatMessage message3 = new ChatMessage();
        message3.setSentAt(LocalDateTime.now());

        assertThat(message1.getSentAt()).isBefore(message2.getSentAt());
        assertThat(message2.getSentAt()).isBefore(message3.getSentAt());
    }

    @Test
    @DisplayName("Should handle different content types")
    void shouldHandleDifferentContentTypes() {
        // Text message
        chatMessage.setContent("Regular text message");
        assertThat(chatMessage.getContent()).isEqualTo("Regular text message");

        // Message with numbers
        chatMessage.setContent("Score: 15-7");
        assertThat(chatMessage.getContent()).isEqualTo("Score: 15-7");

        // Message with emojis (if supported)
        chatMessage.setContent("Good game! 😊");
        assertThat(chatMessage.getContent()).isEqualTo("Good game! 😊");

        // Message with line breaks
        chatMessage.setContent("First line\nSecond line");
        assertThat(chatMessage.getContent()).isEqualTo("First line\nSecond line");
    }

    @Test
    @DisplayName("Should validate message constraints")
    void shouldValidateMessageConstraints() {
        // Test null content
        chatMessage.setContent(null);
        assertThat(chatMessage.getContent()).isNull();

        // Test whitespace-only content
        chatMessage.setContent("   ");
        assertThat(chatMessage.getContent()).isEqualTo("   ");

        // Test content with leading/trailing spaces
        chatMessage.setContent("  message  ");
        assertThat(chatMessage.getContent()).isEqualTo("  message  ");
    }

    @Test
    @DisplayName("Should handle message updates")
    void shouldHandleMessageUpdates() {
        String originalContent = chatMessage.getContent();
        LocalDateTime originalTime = chatMessage.getSentAt();
        
        // Update content
        chatMessage.setContent("Updated message content");
        assertThat(chatMessage.getContent()).isEqualTo("Updated message content");
        assertThat(chatMessage.getContent()).isNotEqualTo(originalContent);
        
        // Timestamp should remain unchanged (typically messages aren't edited)
        assertThat(chatMessage.getSentAt()).isEqualTo(originalTime);
    }

    @Test
    @DisplayName("Should support room-based message grouping")
    void shouldSupportRoomBasedMessageGrouping() {
        // Messages in the same room
        ChatMessage message1 = new ChatMessage();
        message1.setRoom(room);
        message1.setContent("Message 1");

        ChatMessage message2 = new ChatMessage();
        message2.setRoom(room);
        message2.setContent("Message 2");

        assertThat(message1.getRoom()).isEqualTo(message2.getRoom());
        assertThat(message1.getContent()).isNotEqualTo(message2.getContent());
    }

    @Test
    @DisplayName("Should handle different senders in same room")
    void shouldHandleDifferentSendersInSameRoom() {
        User anotherSender = new User();
        anotherSender.setId(3L);
        anotherSender.setUsername("anothersender");
        anotherSender.setEmail("another@example.com");

        ChatMessage message1 = new ChatMessage();
        message1.setRoom(room);
        message1.setSender(sender);
        message1.setContent("Message from sender 1");

        ChatMessage message2 = new ChatMessage();
        message2.setRoom(room);
        message2.setSender(anotherSender);
        message2.setContent("Message from sender 2");

        assertThat(message1.getRoom()).isEqualTo(message2.getRoom());
        assertThat(message1.getSender()).isNotEqualTo(message2.getSender());
        assertThat(message1.getSender().getUsername()).isNotEqualTo(message2.getSender().getUsername());
    }

    @Test
    @DisplayName("Should maintain referential integrity")
    void shouldMaintainReferentialIntegrity() {
        assertThat(chatMessage.getRoom()).isNotNull();
        assertThat(chatMessage.getSender()).isNotNull();
        assertThat(chatMessage.getRoom().getCreatedBy()).isEqualTo(chatMessage.getSender());
    }

    @Test
    @DisplayName("Should handle chronological ordering")
    void shouldHandleChronologicalOrdering() {
        LocalDateTime time1 = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime time2 = LocalDateTime.of(2024, 1, 1, 10, 30, 0);
        LocalDateTime time3 = LocalDateTime.of(2024, 1, 1, 11, 0, 0);

        ChatMessage msg1 = new ChatMessage();
        msg1.setSentAt(time1);

        ChatMessage msg2 = new ChatMessage();
        msg2.setSentAt(time2);

        ChatMessage msg3 = new ChatMessage();
        msg3.setSentAt(time3);

        assertThat(msg1.getSentAt()).isBefore(msg2.getSentAt());
        assertThat(msg2.getSentAt()).isBefore(msg3.getSentAt());
        assertThat(msg1.getSentAt()).isBefore(msg3.getSentAt());
    }

    @Test
    @DisplayName("Should support message content validation")
    void shouldSupportMessageContentValidation() {
        // Test various content scenarios
        String[] testContents = {
            "Simple message",
            "Message with CAPS",
            "Message with 123 numbers",
            "Multi\nline\nmessage",
            "Message with special chars: @#$%",
            "Very long message that goes on and on and on to test the handling of lengthy content in chat messages"
        };

        for (String content : testContents) {
            chatMessage.setContent(content);
            assertThat(chatMessage.getContent()).isEqualTo(content);
        }
    }
}
