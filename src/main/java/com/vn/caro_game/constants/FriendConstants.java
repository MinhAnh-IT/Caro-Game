package com.vn.caro_game.constants;

/**
 * Constants for Friend Management functionality
 * Contains all hardcoded strings and numbers used in friend-related operations
 */
public final class FriendConstants {

    private FriendConstants() {
        // Private constructor to prevent instantiation
    }

    // Success messages
    public static final String FRIEND_REQUEST_SENT_SUCCESS = "Friend request sent successfully";
    public static final String FRIEND_REQUEST_ACCEPTED_SUCCESS = "Friend request accepted successfully";
    public static final String FRIEND_REQUEST_REJECTED_SUCCESS = "Friend request rejected successfully";

    // Relationship status values
    public static final String RELATIONSHIP_STATUS_NONE = "NONE";
    public static final String RELATIONSHIP_STATUS_PENDING = "PENDING";
    public static final String RELATIONSHIP_STATUS_FRIENDS = "FRIENDS";
    public static final String RELATIONSHIP_STATUS_BLOCKED = "BLOCKED";

    // Validation constraints
    public static final int SEARCH_TERM_MIN_LENGTH = 1;
    public static final int SEARCH_TERM_MAX_LENGTH = 50;

    // Validation messages
    public static final String SEARCH_TERM_BLANK_MESSAGE = "Search term cannot be blank";
    public static final String SEARCH_TERM_SIZE_MESSAGE = "Search term must be between 1 and 50 characters";

    // API paths
    public static final String FRIEND_BASE_PATH = "/api/friends";
    public static final String SEARCH_PATH = "/search";
    public static final String REQUEST_PATH = "/request/{userId}";
    public static final String ACCEPT_PATH = "/accept/{userId}";
    public static final String REJECT_PATH = "/reject/{userId}";
    public static final String LIST_PATH = "/list";
    public static final String RECEIVED_REQUESTS_PATH = "/requests/received";
    public static final String SENT_REQUESTS_PATH = "/requests/sent";

    // HTTP Headers
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = 7;
}
