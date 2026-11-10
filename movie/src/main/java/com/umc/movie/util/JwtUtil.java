package com.umc.movie.util;

import com.umc.movie.exception.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey secretKey;
    private final long accessExpiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.access-expiration}") long accessExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        if (accessExpiration <= 0) throw new IllegalArgumentException("JWT 유효시간은 양수여야 합니다.");
        this.accessExpiration = accessExpiration;
    }

    public String createAccessToken(Long memberId, String email) {
        Instant now = Instant.now();
        return Jwts.builder().subject(memberId.toString()).claim("email", email)
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(accessExpiration)))
                .signWith(secretKey).compact();
    }

    public Long getMemberId(String token) {
        try {
            // 같은 서버에서 발급/검증하므로 만료 유예 없이 exp를 검사합니다.
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            if (claims.getExpiration() == null || claims.getSubject() == null) {
                throw new IllegalArgumentException("필수 클레임 누락");
            }
            long memberId = Long.parseLong(claims.getSubject());
            if (memberId <= 0) throw new IllegalArgumentException("잘못된 회원 ID");
            return memberId;
        } catch (ExpiredJwtException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH401_4", "토큰이 만료되었습니다. 다시 로그인해주세요.");
        } catch (JwtException | IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH401_3", "유효하지 않은 토큰입니다.");
        }
    }
}
