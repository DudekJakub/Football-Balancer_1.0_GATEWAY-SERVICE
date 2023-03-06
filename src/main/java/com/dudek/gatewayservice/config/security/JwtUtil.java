package com.dudek.gatewayservice.config.security;

import com.dudek.gatewayservice.model.security.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.token.signature:default}")
    private String key;

    private byte[] getConvertedBinaryKey(String key) {
        String base64Key = DatatypeConverter.printBase64Binary(key.getBytes());
        return DatatypeConverter.parseBase64Binary(base64Key);
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public List<GrantedAuthority> getGrantedAuthoritiesFromToken(String token) {
        return ((List<?>) getClaimFromToken(token, claims -> claims.get("roles")))
                .stream()
                .map(role -> Role.valueOf((String) role)).collect(Collectors.toCollection(ArrayList::new));
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(getConvertedBinaryKey(key)).build().parseClaimsJws(token).getBody();
        } catch (SignatureException | ExpiredJwtException jwtException) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token signature or token is expired! In result this token cannot be trusted.");
        }
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}
