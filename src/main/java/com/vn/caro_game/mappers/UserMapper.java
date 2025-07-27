package com.vn.caro_game.mappers;

import com.vn.caro_game.dtos.request.UpdateProfileRequest;
import com.vn.caro_game.dtos.request.UserCreation;
import com.vn.caro_game.dtos.response.UserProfileResponse;
import com.vn.caro_game.dtos.response.UserResponse;
import com.vn.caro_game.entities.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between User entity and user-related DTOs.
 *
 * <p>This mapper follows the Single Responsibility Principle by handling only
 * user-related data transformations. It provides methods for converting entities
 * to response DTOs and applying request DTOs to entities.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@Component
public class UserMapper {

    /**
     * Converts UserCreation DTO to User entity for registration.
     *
     * @param userCreation the user creation request
     * @return User entity (without password - should be set separately)
     */
    public User toEntity(UserCreation userCreation) {
        if (userCreation == null) {
            return null;
        }

        User user = new User();
        user.setUsername(userCreation.getUsername());
        user.setDisplayName(userCreation.getDisplayName());
        user.setEmail(userCreation.getEmail());
        // Note: password should be encoded and set separately in service layer

        return user;
    }

    /**
     * Converts User entity to UserResponse DTO.
     *
     * @param user the user entity to convert
     * @return UserResponse DTO for authentication responses
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Converts User entity to UserProfileResponse DTO.
     *
     * @param user the user entity to convert
     * @return UserProfileResponse DTO containing user profile information
     */
    public UserProfileResponse toProfileResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Updates User entity with data from UpdateProfileRequest.
     *
     * <p>This method only updates the fields that are provided in the request.
     * It does not modify sensitive fields like password or email.</p>
     *
     * @param user the user entity to update
     * @param request the update request containing new profile data
     */
    public void updateUserFromRequest(User user, UpdateProfileRequest request) {
        if (user == null || request == null) {
            return;
        }

        user.setUsername(request.getUsername());
        user.setDisplayName(request.getDisplayName());
    }
}
