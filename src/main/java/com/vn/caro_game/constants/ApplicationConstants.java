package com.vn.caro_game.constants;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class ApplicationConstants {
    
    // Token Configuration
    public static final long DEFAULT_ACCESS_TOKEN_EXPIRATION = 3600L; // 1 hour in seconds
    public static final int BEARER_TOKEN_START_INDEX = 7;
}
