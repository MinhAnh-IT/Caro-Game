package com.vn.caro_game.integrations.jwt;

import com.vn.caro_game.entities.User;

public interface JwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    String extractEmail(String token);
    Long extractUserId(String token);
    boolean isTokenValid(String token, String email);
    boolean isTokenExpired(String token);
    void invalidateToken(String token);
    boolean isTokenBlacklisted(String token);
}
