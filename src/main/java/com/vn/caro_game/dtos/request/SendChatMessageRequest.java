package com.vn.caro_game.dtos.request;

import com.vn.caro_game.constants.GameRoomConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for sending a chat message in a room.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request data for sending a chat message")
public class SendChatMessageRequest {

    @NotBlank(message = "Message content is required")
    @Size(max = GameRoomConstants.MAX_CHAT_MESSAGE_LENGTH, 
          message = "Message cannot exceed 500 characters")
    @Schema(description = "Chat message content", 
            example = "Good luck with the game!", 
            maxLength = GameRoomConstants.MAX_CHAT_MESSAGE_LENGTH)
    String content;
}
