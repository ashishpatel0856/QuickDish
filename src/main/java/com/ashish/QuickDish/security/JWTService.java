package com.ashish.QuickDish.security;

import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.Entity.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JWTService {

    @Value("${jwt.SecretKey}")
    private String jwtSecretKey;
//cd
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles()
                        .stream().map(Role::name)
                        .collect(Collectors.toList()))
                .claim("name", user.getName())
                .claim("isVerified", user.getIsVerified())

                // Rider specific claims
                .claim("isRider", user.getRoles().contains(Role.ROLE_RIDER))
                .claim("riderVerified", user.getRiderProfile() != null ? user.getRiderProfile().getIsVerifiedRider() : false)
                .claim("riderStatus", user.getRiderProfile() != null && user.getRiderProfile().getStatus() != null ?
                        user.getRiderProfile().getStatus().name() : null)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 90)) // 3 months
                .signWith(getSecretKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles()
                        .stream().map(Role::name)
                        .collect(Collectors.toList()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30 * 6)) // 6 months
                .signWith(getSecretKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token is missing or empty");
        }
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }

//    public String getEmailFromToken(String token) {
//        Claims claims = extractAllClaims(token);
//        return claims.get("email", String.class);
//    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("roles");
    }

//    public boolean isRider(String token) {
//        List<String> roles = getRolesFromToken(token);
//        return roles != null && roles.contains("ROLE_RIDER");
//    }
//
//    public boolean isAdmin(String token) {
//        List<String> roles = getRolesFromToken(token);
//        return roles != null && roles.contains("ROLE_ADMIN");
//    }
//
//    public boolean isUser(String token) {
//        List<String> roles = getRolesFromToken(token);
//        return roles != null && roles.contains("ROLE_USER");
//    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

//    public boolean isTokenValid(String token, User user) {
//        final Long userId = getUserIdFromToken(token);
//        return (userId.equals(user.getId()) && !isTokenExpired(token));
//    }

    private boolean isTokenExpired(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getExpiration().before(new Date());
    }


}