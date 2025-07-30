package com.vn.caro_game.configs;

import com.vn.caro_game.entities.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetails implementation that wraps User entity
 * and provides authentication information for Spring Security.
 *
 * <p>This class implements UserDetails interface and provides
 * user authentication and authorization details including:
 * <ul>
 *   <li>User ID - for internal identification</li>
 *   <li>Email - for authentication and contact</li>
 *   <li>Username - for login</li>
 *   <li>Password - for authentication</li>
 *   <li>Account status flags</li>
 * </ul>
 *
 * @author Caro Game Team
 * @since 1.0.0
 * @see UserDetails
 * @see User
 */
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomUserDetails implements UserDetails {

    User user;

    /**
     * Gets the user ID from the wrapped User entity.
     *
     * @return the user's unique identifier
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * Gets the email from the wrapped User entity.
     *
     * @return the user's email address
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * Gets the display name from the wrapped User entity.
     *
     * @return the user's display name or null if not set
     */
    public String getDisplayName() {
        return user.getDisplayName();
    }

    /**
     * Gets the avatar URL from the wrapped User entity.
     *
     * @return the user's avatar URL or null if not set
     */
    public String getAvatarUrl() {
        return user.getAvatarUrl();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Default role for all users - can be expanded later
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
}
