package com.thewealthweb.crmbackend.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final String jwtSecret = "619e6b668204c646dec19dce39ba9d70f7a587f0a661fd6ead36a084a6a47f3ccdd60ff78ab1319743122d0e21479e0e71e17ca5a70535ae52f607f470f4e109f13f71bc3cabdfabc3dc177007770e24a8d60e07ba4284123bf547fae0e7b5a203dcedeead0635c75a97437e68b68a55e6a8fae49693b8d30646890028a007553198391b7f62e38751258d3747fb74b8884b3304b2026f1d3ca1c5f52d13bca4869f301d119f92ab268755aa3e949206d1ba2be203636ac87348868c996fb89410b3b642cc278e3680d81cf9ff8bd6cfc89577f20a45ad697ca6601b46c95f229e8f0c21b32ee06cd681764493f9dd1c657cb5ea26aa6cf67ab34585d035769c";

    public String generateToken(UserDetails userDetails) {
        // 1 hour
        long jwtExpirationInMs = 3600000;
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Invalid JWT: " + e.getMessage());
        }
        return false;
    }
}
