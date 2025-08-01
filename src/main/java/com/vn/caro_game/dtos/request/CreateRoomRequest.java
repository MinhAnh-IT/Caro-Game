package com.vn.caro_game.dtos.request;

import com.vn.caro_game.constants.GameRoomConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for creating a new game room.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request data for creating a new game room")
public class CreateRoomRequest {

    @NotBlank(message = "Room name is required")
    @Size(min = GameRoomConstants.MIN_ROOM_NAME_LENGTH, 
          max = GameRoomConstants.MAX_ROOM_NAME_LENGTH, 
          message = "Room name must be between 3 and 100 characters")
    @Schema(description = "Name of the game room", 
            example = "Epic Caro Battle", 
            minLength = GameRoomConstants.MIN_ROOM_NAME_LENGTH,
            maxLength = GameRoomConstants.MAX_ROOM_NAME_LENGTH)
    String name;

    @NotNull(message = "Room privacy setting is required")
    @Schema(description = "Whether the room is private (requires join code) or public", 
            example = "false")
    Boolean isPrivate;
}
