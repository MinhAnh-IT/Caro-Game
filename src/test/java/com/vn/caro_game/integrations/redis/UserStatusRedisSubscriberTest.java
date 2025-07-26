package com.vn.caro_game.integrations.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStatusRedisSubscriberTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private Message message;

    private UserStatusRedisSubscriber userStatusRedisSubscriber;

    @BeforeEach
    void setUp() {
        userStatusRedisSubscriber = new UserStatusRedisSubscriber(messagingTemplate);
    }

    @Test
    void testOnMessageString_WithValidMessage() {
        // Given
        String channel = "__keyspace@0__:user:status:123";
        String messageContent = "User status updated";

        // When
        userStatusRedisSubscriber.onMessage(channel, messageContent);

        // Then
        verify(messagingTemplate).convertAndSend("/topic/user-status", messageContent);
    }

    @Test
    void testOnMessageString_WithNullChannel() {
        // Given
        String messageContent = "User status updated";

        // When
        userStatusRedisSubscriber.onMessage(null, messageContent);

        // Then
        verify(messagingTemplate).convertAndSend("/topic/user-status", messageContent);
    }

    @Test
    void testOnMessageString_WithEmptyMessage() {
        // Given
        String channel = "__keyspace@0__:user:status:123";
        String messageContent = "";

        // When
        userStatusRedisSubscriber.onMessage(channel, messageContent);

        // Then
        verify(messagingTemplate).convertAndSend("/topic/user-status", messageContent);
    }

    @Test
    void testOnMessageWithMessageObject_ValidPattern() {
        // Given
        String messageBody = "User online";
        String pattern = "__keyspace@0__:user:status:*";
        byte[] patternBytes = pattern.getBytes();
        
        when(message.getBody()).thenReturn(messageBody.getBytes());

        // When
        userStatusRedisSubscriber.onMessage(message, patternBytes);

        // Then
        verify(messagingTemplate).convertAndSend("/topic/user-status", messageBody);
    }

    @Test
    void testOnMessageWithMessageObject_NullPattern() {
        // Given
        String messageBody = "User offline";
        
        when(message.getBody()).thenReturn(messageBody.getBytes());

        // When
        userStatusRedisSubscriber.onMessage(message, null);

        // Then
        verify(messagingTemplate).convertAndSend("/topic/user-status", messageBody);
    }

    @Test
    void testOnMessageWithMessageObject_EmptyMessageBody() {
        // Given
        String pattern = "__keyspace@0__:user:status:*";
        byte[] patternBytes = pattern.getBytes();
        
        when(message.getBody()).thenReturn(new byte[0]);

        // When
        userStatusRedisSubscriber.onMessage(message, patternBytes);

        // Then
        verify(messagingTemplate).convertAndSend("/topic/user-status", "");
    }
}
