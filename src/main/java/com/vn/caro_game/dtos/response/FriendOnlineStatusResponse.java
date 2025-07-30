package com.vn.caro_game.dtos.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Response DTO for friend online status information.
 *
 * <p>This DTO contains essential information about a friend including their
 * online status, display details, and avatar. Used in responses when fetching
 * the online status of a user's friends list.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendOnlineStatusResponse {

    /**
     * The unique identifier of the friend user.
     */
    Long userId;

    /**
     * The display name of the friend.
     */
    String displayName;

    /**
     * The avatar URL of the friend.
     */
    String avatarUrl;

    /**
     * The online status of the friend.
     * true if the friend is currently online, false otherwise.
     */
    Boolean status;
}
