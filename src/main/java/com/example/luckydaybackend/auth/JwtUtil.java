package com.example.luckydaybackend.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "iwbgi8EJEx7khxRx+AhlAJKhAqyl6kN463+FHezVOCk="; // 반드시 환경 변수로 관리!

    private static final long ACCESS_EXPIRATION = 1000 * 60 * 15; // 액세스 토큰 만료: 15분
    private static final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 리프레시 토큰 만료: 7일

    // Base64 디코딩해서 서명 키 가져오기
    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 액세스 토큰 생성
    public String generateAccessToken(Long id, String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("id", id)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    // 리프레시 토큰 생성
    public String generateRefreshToken(Long id, String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("id", id)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    // 토큰에서 email 추출
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // ✅ 토큰에서 ID 추출
    public Long extractId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id", Long.class);
    }



    // ✅ 토큰 검증 (만료 여부 포함)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
//            System.out.println("✅ 토큰 검증 성공: " + token);
            return true;
        } catch (Exception e) {
            System.err.println("🚨 JWT 검증 실패: " + e.getMessage());
            return false;
        }
    }


    // ✅ 토큰 만료 여부 체크
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // ✅ JWT 토큰을 SHA-256 해시로 변환 (user_sessions 테이블에서 찾을 때 사용)
    public String hashToken(String token) {
        String hashedToken = DigestUtils.sha256Hex(token);
//        System.out.println("✅ 해싱된 토큰 값: " + hashedToken);
        return hashedToken;
    }

}
