package com.umc.study.util;

import com.umc.study.apiPayload.code.ErrorCode;
import com.umc.study.apiPayload.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/** JWT 발급과 검증을 담당하는 도구 클래스 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final Duration accessExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") Long accessExpiration
    ) {
        // 문자열 비밀키 → HMAC-SHA 서명용 키 (32바이트 미만이면 여기서 WeakKeyException)
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = Duration.ofSeconds(accessExpiration);
    }

    /** 로그인에 성공한 회원에게 발급할 Access Token */
    public String createAccessToken(Long memberId, String email) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(memberId))                  // 토큰의 주인 = memberId
                .claim("email", email)                              // 참고용 부가 정보 (비밀번호 같은 민감 정보 금지)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessExpiration)))  // 만료 시각이 없으면 영원히 유효
                .signWith(secretKey)                                // 내용을 조작하면 서명 검증에서 걸림
                .compact();
    }

    /**
     * 토큰을 검증하고 memberId를 꺼냅니다.
     * 값을 돌려줬다면 "서명과 만료까지 검증을 통과한 토큰"이라는 뜻입니다.
     */
    public Long getMemberId(String token) {
        try {
            return Long.valueOf(parseClaims(token).getSubject());
        } catch (ExpiredJwtException e) {
            // 만료는 다시 로그인하면 해결되므로 따로 구분
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            // 서명 불일치, 형식 오류, sub가 숫자가 아님(NumberFormatException) 등
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)   // 같은 키로 서명을 다시 계산해 대조
                .clockSkewSeconds(60)    // 서버 간 시계 오차 60초 허용
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
