package mafia.server.web.auth.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import mafia.server.web.auth.AccountContext;
import mafia.server.web.auth.AccountDetails;
import mafia.server.web.auth.AccountDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtProvider {

    private final String TOKEN_TYPE = "Bearer";
    private final SecretKey JWT_SECRET_KEY;
    private final long EXPIRATION_TIME;
    private final long REFRESH_EXPIRATION_TIME;

    public JwtProvider(
            @Value("${jwt.secret_key}") String jwtSecretKey,
            @Value("${jwt.expiration_time:3600}") long expirationTime,
            @Value("${jwt.refresh_expiration_time:1209600}") long refreshExpirationTime
    ) {
        JWT_SECRET_KEY = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
        EXPIRATION_TIME = expirationTime;
        REFRESH_EXPIRATION_TIME = refreshExpirationTime;
    }

    public String generateToken(AccountDetails accountDetails) {
        return generateToken(accountDetails, false);
    }

    public String generateRefreshToken(AccountDetails accountDetails) {
        return generateToken(accountDetails, true);
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public AccountContext getAccountContextFromToken(String token) {
        if (!validateToken(token)) {
            throw new IllegalArgumentException("Invalid Token: " + token);
        }
        Claims claims = getClaims(token);
        return new AccountContext(
                claims.get("accountId", Long.class),
                claims.get("authorities", List.class).stream()
                        .map(auth -> new SimpleGrantedAuthority(auth.toString()))
                        .toList()
        );
    }

    private String generateToken(AccountDetails accountDetails, boolean isRefresh) {
        AccountDto accountDto = accountDetails.getAccountDto();
        return Jwts.builder()
                .subject(String.valueOf(accountDto.id()))
                .claim("accountId", accountDto.id())
                .claim(
                        "authorities", accountDetails.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority).toList()
                )
                .signWith(JWT_SECRET_KEY)
                .expiration(createExpiration(isRefresh ? REFRESH_EXPIRATION_TIME : EXPIRATION_TIME))
                .compact();
    }

    private Date createExpiration(long seconds) {
        return new Date(System.currentTimeMillis() + (seconds * 1000));
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(JWT_SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getTokenType() {
        return TOKEN_TYPE;
    }

    public long getExpirationTime() {
        return EXPIRATION_TIME;
    }
}
