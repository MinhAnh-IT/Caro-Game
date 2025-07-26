package com.vn.caro_game.integrations.jwt;

import com.vn.caro_game.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * JWT Service implementation for token management and validation.
 * 
 * <p>This service provides comprehensive JWT token operations including generation,
 * validation, blacklisting, and claim extraction. It integrates with Redis for
 * token blacklisting and supports both access and refresh tokens.</p>
 * 
 * <h3>Token Structure:</h3>
 * <ul>
 *   <li><strong>Subject:</strong> User's email address</li>
 *   <li><strong>Claims:</strong> userId, username (for access tokens)</li>
 *   <li><strong>Security:</strong> HMAC SHA-256 signature</li>
 * </ul>
 * 
 * <h3>Token Types:</h3>
 * <ul>
 *   <li><strong>Access Token:</strong> Short-lived, contains user claims</li>
 *   <li><strong>Refresh Token:</strong> Long-lived, minimal claims</li>
 * </ul>
 * 
 * <h3>Security Features:</h3>
 * <ul>
 *   <li>Token blacklisting in Redis for logout functionality</li>
 *   <li>Automatic token expiration handling</li>
 *   <li>Bearer token prefix support</li>
 *   <li>Secure token validation with exception handling</li>
 * </ul>
 * 
 * @author Caro Game Team
 * @since 1.0.0
 * @see JwtService
 * @see User
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtServiceImpl implements JwtService {
    
    final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${jwt.secret}")
    String secretKey;
    
    @Value("${jwt.access-token.expiration}")
    Long accessTokenExpiration;
    
    @Value("${jwt.refresh-token.expiration}")
    Long refreshTokenExpiration;
    
    static final String BLACKLIST_PREFIX = "blacklist:";
    
    @Override
    public String generateAccessToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("username", user.getUsername());
        
        return generateToken(extraClaims, user.getEmail(), accessTokenExpiration);
    }
    
    @Override
    public String generateRefreshToken(User user) {
        return generateToken(new HashMap<>(), user.getEmail(), refreshTokenExpiration);
    }
    
    @Override
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    @Override
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }
    
    @Override
    public boolean isTokenValid(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email)) && !isTokenExpired(token) && !isTokenBlacklisted(token);
    }
    
    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    @Override
    public void invalidateToken(String token) {
        Date expiration = extractExpiration(token);
        long ttl = expiration.getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token, 
                "blacklisted", 
                ttl, 
                TimeUnit.MILLISECONDS
            );
        }
    }
    
    @Override
    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
    
    // Implementation from JwtUtil
    @Override
    public Long getUserIdFromToken(String token) {
        Claims claims = extractAllClaims(cleanToken(token));
        return claims.get("userId", Long.class);
    }
    
    @Override
    public boolean validateToken(String token) {
        try {
            extractAllClaims(cleanToken(token));
            return !isTokenBlacklisted(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    // Helper method to clean token (remove "Bearer " prefix if present)
    private String cleanToken(String token) {
        return token.replace("Bearer ", "");
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private String generateToken(Map<String, Object> extraClaims, String subject, Long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(cleanToken(token))
                .getBody();
    }
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
