package com.vn.caro_game.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Response DTO for chat message information.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Chat message information")
public class ChatMessageResponse {

    @Schema(description = "Message ID", example = "1")
    Long id;

    @Schema(description = "Message sender information")
    UserSummaryResponse sender;

    @Schema(description = "Message content", example = "Good luck with the game!")
    String content;

    @Schema(description = "Time when message was sent", example = "2024-01-15T10:30:00")
    LocalDateTime sentAt;
}
