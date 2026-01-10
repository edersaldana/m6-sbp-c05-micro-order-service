package com.orden.service.orden_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.function.Function;

@Service
public class JwtService {

    private final Key key;

    // Leemos la misma propiedad que usa el user-service
    public JwtService(@Value("${jwt.secret}") String secret) {
        // IMPORTANTE: Usamos Decoders.BASE64 para que coincida con la llave del user-service
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el userId que el user-service incluyó en el claim
     */
    public Long extractUserId(String token) {
        final Claims claims = extractAllClaims(cleanToken(token));
        // El user-service lo guarda como "userId"
        return claims.get("userId", Long.class);
    }

    public String extractUsername(String token) {
        return extractClaim(cleanToken(token), Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(cleanToken(token));
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(cleanToken(token));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Utilidad para limpiar el prefijo Bearer si viene en el string
     */
    private String cleanToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}