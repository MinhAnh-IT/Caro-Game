package com.vn.caro_game.constants;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Constants for user profile related operations.
 *
 * <p>This class contains all constant values used throughout the user profile
 * management functionality including success messages, error messages, and
 * configuration values.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class UserProfileConstants {
    
    // Success Messages
    public static final String USER_PROFILE_RETRIEVED_SUCCESS = "User profile retrieved successfully";
    public static final String PROFILE_UPDATED_SUCCESS = "Profile updated successfully";
    public static final String AVATAR_UPLOADED_SUCCESS = "Avatar uploaded successfully";
    
    // Error Messages
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USERNAME_ALREADY_EXISTS = "Username already exists";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    public static final String INVALID_FILE_FORMAT = "Invalid file format. Only JPEG, PNG, and GIF are supported";
    public static final String FILE_SIZE_EXCEEDED = "File size exceeds maximum limit of 5MB";
    public static final String FILE_UPLOAD_FAILED = "File upload failed due to server error";
    public static final String AVATAR_UPDATE_FAILED = "Failed to update avatar";
    public static final String PROFILE_UPDATE_FAILED = "Failed to update profile";
    
    // File Upload Configuration
    public static final long MAX_AVATAR_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif"};
    public static final String[] ALLOWED_FILE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};
    
    // Avatar Configuration
    public static final int RECOMMENDED_AVATAR_WIDTH = 200;
    public static final int RECOMMENDED_AVATAR_HEIGHT = 200;
    public static final String AVATAR_UPLOAD_PATH = "/uploads/avatars/";
    public static final String DEFAULT_AVATAR_FILENAME = "default_avatar.png";
    
    // Validation Messages
    public static final String USERNAME_REQUIRED = "Username is required";
    public static final String USERNAME_MIN_LENGTH = "Username must be at least 3 characters long";
    public static final String USERNAME_MAX_LENGTH = "Username must not exceed 50 characters";
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String EMAIL_INVALID_FORMAT = "Invalid email format";
    public static final String DISPLAY_NAME_REQUIRED = "Display name is required";
    public static final String DISPLAY_NAME_MAX_LENGTH = "Display name must not exceed 100 characters";
    
    // API Operation IDs
    public static final String GET_USER_PROFILE_OPERATION_ID = "getCurrentUserProfile";
    public static final String UPDATE_USER_PROFILE_OPERATION_ID = "updateUserProfile";
    public static final String UPLOAD_AVATAR_OPERATION_ID = "uploadUserAvatar";
    public static final String UPDATE_COMPLETE_PROFILE_OPERATION_ID = "updateCompleteProfile";
    
    // Request Parameter Names
    public static final String AVATAR_PARAM_NAME = "avatar";
    public static final String PROFILE_PARAM_NAME = "profile";
}
