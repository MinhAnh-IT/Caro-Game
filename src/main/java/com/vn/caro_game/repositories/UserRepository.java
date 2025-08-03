package com.vn.caro_game.repositories;

import com.vn.caro_game.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, Long id);

    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.email = :email")
    void updatePassword(@Param("email") String email, @Param("password") String password);

    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "u.id != :currentUserId")
    List<User> findUsersByDisplayNameOrUsername(@Param("searchTerm") String searchTerm,
                                               @Param("currentUserId") Long currentUserId);

    /**
     * Finds users who have game history (either won or lost games)
     */
    @Query("SELECT DISTINCT u FROM User u WHERE " +
           "u.id IN (SELECT gh.winnerId FROM GameHistory gh WHERE gh.winnerId IS NOT NULL) OR " +
           "u.id IN (SELECT gh.loserId FROM GameHistory gh WHERE gh.loserId IS NOT NULL)")
    List<User> findUsersWithGameHistory();
}
