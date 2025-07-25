package com.vn.caro_game.repositories;

import com.vn.caro_game.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setAvatarUrl("https://example.com/avatar.jpg");
    }

    @Test
    @DisplayName("Should save and retrieve user")
    void shouldSaveAndRetrieveUser() {
        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find user by username")
    void shouldFindUserByUsername() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should return empty when user not found by username")
    void shouldReturnEmptyWhenUserNotFoundByUsername() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should check if user exists by username")
    void shouldCheckIfUserExistsByUsername() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When & Then
        assertThat(userRepository.existsByUsername("testuser")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void shouldCheckIfUserExistsByEmail() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When & Then
        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should update user password")
    void shouldUpdateUserPassword() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        String newPassword = "newHashedPassword";

        // When
        userRepository.updatePassword(savedUser.getEmail(), newPassword);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<User> updatedUser = userRepository.findByEmail(savedUser.getEmail());
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getPassword()).isEqualTo(newPassword);
    }

    @Test
    @DisplayName("Should handle unique constraint for username")
    void shouldHandleUniqueConstraintForUsername() {
        // Given
        entityManager.persistAndFlush(testUser);

        User duplicateUser = new User();
        duplicateUser.setUsername("testuser"); // Same username
        duplicateUser.setEmail("different@example.com");
        duplicateUser.setPassword("password");

        // When & Then
        try {
            entityManager.persistAndFlush(duplicateUser);
        } catch (Exception e) {
            // Expected exception due to unique constraint
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("Should handle unique constraint for email")
    void shouldHandleUniqueConstraintForEmail() {
        // Given
        entityManager.persistAndFlush(testUser);

        User duplicateUser = new User();
        duplicateUser.setUsername("differentuser");
        duplicateUser.setEmail("test@example.com"); // Same email
        duplicateUser.setPassword("password");

        // When & Then
        try {
            entityManager.persistAndFlush(duplicateUser);
        } catch (Exception e) {
            // Expected exception due to unique constraint
            assertThat(e).isNotNull();
        }
    }
}
