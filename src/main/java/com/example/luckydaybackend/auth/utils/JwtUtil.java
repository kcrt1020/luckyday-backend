package com.example.luckydaybackend.auth.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "iwbgi8EJEx7khxRx+AhlAJKhAqyl6kN463+FHezVOCk="; // 반드시 환경 변수로 관리!
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24시간

    // ✅ Base64 디코딩해서 서명 키 가져오기
    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY); // Base64 디코딩 추가
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ✅ JWT 토큰 생성
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ JWT에서 email 추출
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // ✅ JWT에서 Claims 추출 (parser() → parserBuilder() 변경)
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // ✅ getSigningKey() 사용
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ 토큰 검증 시 예외 메시지 출력 추가
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.err.println("🚨 JWT 검증 실패: " + e.getMessage()); // ✅ 예외 메시지 출력
            return false;
        }
    }
}
