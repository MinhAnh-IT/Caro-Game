package com.vn.caro_game.dtos;

import com.vn.caro_game.constants.FriendConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request DTO for searching users to send friend requests")
public class FriendRequestDto {

    @NotBlank(message = FriendConstants.SEARCH_TERM_BLANK_MESSAGE)
    @Size(
        min = FriendConstants.SEARCH_TERM_MIN_LENGTH,
        max = FriendConstants.SEARCH_TERM_MAX_LENGTH,
        message = FriendConstants.SEARCH_TERM_SIZE_MESSAGE
    )
    @Schema(
        description = "Search term for finding users by display name or username",
        example = "john_doe",
        minLength = FriendConstants.SEARCH_TERM_MIN_LENGTH,
        maxLength = FriendConstants.SEARCH_TERM_MAX_LENGTH
    )
    String searchTerm;
}
