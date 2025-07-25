package com.vn.caro_game.constants;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class HttpStatusConstants {
    
    // Success Status Codes
    public static final int OK = 200;
    
    // Client Error Status Codes
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int TOO_MANY_REQUESTS = 429;
    
    // Server Error Status Codes
    public static final int INTERNAL_SERVER_ERROR = 500;
}
