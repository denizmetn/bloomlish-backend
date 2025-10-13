package com.deniz.bloomlishbackend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret.key}")
    private final String SECRET_KEY;

    public JwtService(@Value("${jwt.secret.key}") String secretKey) {
        this.SECRET_KEY = secretKey;
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        claims.put("roles", roles);
        if (userDetails instanceof com.deniz.bloomlishbackend.entity.User user) {
            claims.put("id", user.getUserID());
        }
        return generateToken(claims, userDetails);

    }

    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {//Token süresi bugünden önceyse true döner.
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {//Token içindeki expiration (bitiş tarihi) claim'ini döner.
        return extractClaim(token, Claims::getExpiration);
    }
    public Long extractId(String token) {
        return extractAllClaims(token).get("id", Long.class);
    }

    public String extractUsername(String token) {// kullanıcı adı alanını alır. JWT token içinden subject olarak saklanan kullanıcı adını (username) çıkarmaya yarar.
        return extractClaim(token,Claims::getSubject); // claim'in subject (yani kullanıcı adı) kısmını döndür.
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims); //Token içinden herhangi bir alanı (claim) almak için genelleştirilmiş bir yöntem.
    }
}
