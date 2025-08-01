package com.vn.caro_game.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for inviting a friend to a room.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request data for inviting a friend to room")
public class InviteFriendRequest {

    @NotNull(message = "Friend user ID is required")
    @Schema(description = "ID of the friend to invite", 
            example = "123")
    Long friendUserId;
}
