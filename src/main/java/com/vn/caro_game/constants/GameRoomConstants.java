package com.vn.caro_game.constants;

/**
 * Constants for Game Room functionality.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
public final class GameRoomConstants {
    
    private GameRoomConstants() {
        // Utility class
    }
    
    // Room limits
    public static final int MAX_PLAYERS_PER_ROOM = 2;
    public static final int JOIN_CODE_LENGTH = 4;
    
    // Game time limits (in seconds)
    public static final int GAME_TIME_LIMIT_SECONDS = 600; // 10 minutes
    public static final int MOVE_TIME_LIMIT_SECONDS = 30; // 30 seconds per move
    
    // Redis TTL
    public static final long ROOM_CACHE_TTL_SECONDS = 3600; // 1 hour
    public static final long GAME_TIMER_TTL_SECONDS = GAME_TIME_LIMIT_SECONDS + 60; // Buffer time
    
    // WebSocket topics
    public static final String TOPIC_ROOM_UPDATE = "/topic/room/";
    public static final String TOPIC_ROOM_CHAT = "/topic/room/%d/chat";
    public static final String TOPIC_GAME_UPDATE = "/topic/game/";
    
    // Redis channels
    public static final String REDIS_CHANNEL_ROOM_UPDATE = "room-update";
    public static final String REDIS_CHANNEL_GAME_TIMER = "game-timer";
    
    // Room name constraints
    public static final int MIN_ROOM_NAME_LENGTH = 3;
    public static final int MAX_ROOM_NAME_LENGTH = 100;
    
    // Chat message constraints
    public static final int MAX_CHAT_MESSAGE_LENGTH = 500;
    
    // Join code pattern
    public static final String JOIN_CODE_PATTERN = "^[A-Z0-9]{4}$";
    
    // Join code generation characters
    public static final String JOIN_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
}
