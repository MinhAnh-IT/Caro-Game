package com.vn.caro_game.configs;

import com.vn.caro_game.entities.User;
import com.vn.caro_game.repositories.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService implementation for loading user-specific data.
 *
 * <p>This service loads user details from the database using the UserRepository
 * and wraps them in a CustomUserDetails object for Spring Security authentication.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 * @see UserDetailsService
 * @see CustomUserDetails
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomUserDetailsService implements UserDetailsService {

    UserRepository userRepository;

    /**
     * Loads user details by username (email in this case).
     *
     * @param username the email of the user to load
     * @return UserDetails object containing user information
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with email: " + username));

        return new CustomUserDetails(user);
    }
}
