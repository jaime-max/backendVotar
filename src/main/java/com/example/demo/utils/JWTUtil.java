package com.example.demo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;  // Importación de Base64
import java.util.Date;

@Component
public class JWTUtil {
    @Value("${security.jwt.secret}")
    private String key;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.ttlMillis}")
    private long ttlMillis;

    private final Logger log = LoggerFactory.getLogger(JWTUtil.class);

    /**
     * Create a new token.
     *
     * @param id
     * @param subject
     * @return
     */
    public String create(String id, String subject) {

        // The JWT signature algorithm used to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        // Sign JWT with our ApiKey secret
        byte[] apiKeySecretBytes = Base64.getDecoder().decode(key); // Reemplazado DatatypeConverter
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        // Set the JWT Claims
        JwtBuilder builder = Jwts.builder().setId(id).setIssuedAt(now).setSubject(subject).setIssuer(issuer)
                .signWith(signatureAlgorithm, signingKey);

        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        // Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    /**
     * Method to validate and read the JWT
     *
     * @param jwt
     * @return
     */
    public String extractUsername(String jwt) {
        Claims claims = Jwts.parser().setSigningKey(Base64.getDecoder().decode(key))  // Reemplazado DatatypeConverter
                .parseClaimsJws(jwt).getBody();
        return claims.getSubject(); // Devuelve el 'subject' que se debe configurar como nombre de usuario
    }

    /**
     * Method to validate and read the JWT
     *
     * @param jwt
     * @return
     */
    public String extractUserId(String jwt) {
        Claims claims = Jwts.parser().setSigningKey(Base64.getDecoder().decode(key))  // Reemplazado DatatypeConverter
                .parseClaimsJws(jwt).getBody();
        return claims.getId(); // Devuelve el 'id', que podría ser el ID del usuario
    }

    public boolean validateToken(String jwt, UserDetails userDetails) {
        String username = extractUsername(jwt);  // Extrae el nombre de usuario
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(jwt));
    }

    private boolean isTokenExpired(String jwt) {
        Date expirationDate = Jwts.parser()
                .setSigningKey(Base64.getDecoder().decode(key))  // Reemplazado DatatypeConverter
                .parseClaimsJws(jwt)
                .getBody()
                .getExpiration();
        return expirationDate.before(new Date());
    }
}
