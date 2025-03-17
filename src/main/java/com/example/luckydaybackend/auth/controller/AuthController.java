package com.example.luckydaybackend.auth.controller;

import com.example.luckydaybackend.auth.dto.LoginRequest;
import com.example.luckydaybackend.auth.model.User;
import com.example.luckydaybackend.auth.repository.UserRepository;
import com.example.luckydaybackend.auth.utils.JwtUtil;
import com.example.luckydaybackend.model.UserSession;
import com.example.luckydaybackend.repository.UserSessionRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserSessionRepository userSessionRepository;

    public AuthController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserSessionRepository userSessionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userSessionRepository = userSessionRepository;
    }

    // ✅ 로그인 API (기존 세션 삭제 후 새 토큰 저장)
    @Transactional
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        logger.info("🔥 로그인 요청 - 이메일: " + request.getEmail());

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty() || !passwordEncoder.matches(request.getPassword(), optionalUser.get().getPasswordHash())) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "잘못된 이메일 또는 비밀번호입니다."));
        }

        User user = optionalUser.get();

        // ✅ 기존 세션 삭제
        userSessionRepository.deleteByUserId(user.getId());

        // ✅ 새 리프레시 토큰 발급
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // ✅ 리프레시 토큰을 저장
        UserSession session = new UserSession();
        session.setUserId(user.getId());
        session.setTokenHash(DigestUtils.sha256Hex(refreshToken));
        session.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        userSessionRepository.save(session);

        logger.info("✅ 로그인 성공 - 이메일: " + user.getEmail());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("accessToken", jwtUtil.generateAccessToken(user.getEmail()), "refreshToken", refreshToken));
    }



    // ✅ 리프레시 토큰을 이용해 액세스 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        if (!request.containsKey("refreshToken") || request.get("refreshToken") == null) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "리프레시 토큰이 제공되지 않았습니다."));
        }

        String refreshToken = request.get("refreshToken");
        logger.info("🔄 클라이언트가 보낸 리프레시 토큰: " + refreshToken);

        String email = jwtUtil.extractEmail(refreshToken);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "유효하지 않은 리프레시 토큰"));
        }

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "유저를 찾을 수 없습니다."));
        }

        String tokenHash = jwtUtil.hashToken(refreshToken);
        logger.info("✅ 요청 시 해싱된 리프레시 토큰: " + tokenHash);

        Optional<UserSession> session = userSessionRepository.findByUserIdAndTokenHash(user.get().getId(), tokenHash);

        if (session.isEmpty()) {
            logger.error("🚨 세션 정보 없음! 저장된 토큰 해시와 일치하지 않음.");
            logger.info("🔍 DB에 저장된 토큰 해시 조회 중...");

            // 🔍 DB에 저장된 모든 `token_hash` 조회
            List<UserSession> allSessions = userSessionRepository.findByUserId(user.get().getId());
            for (UserSession s : allSessions) {
                logger.info("🔍 DB에 저장된 토큰 해시: " + s.getTokenHash());
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "유효하지 않은 리프레시 토큰"));
        }

        String newAccessToken = jwtUtil.generateAccessToken(email);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("accessToken", newAccessToken));
    }


    // ✅ 로그아웃 API (리프레시 토큰 삭제)
    @PostMapping("/logout")
    @Transactional // ✅ 트랜잭션 추가
    public ResponseEntity<String> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        String email = jwtUtil.extractEmail(refreshToken);

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
        }

        String tokenHash = jwtUtil.hashToken(refreshToken);
        userSessionRepository.deleteByUserIdAndTokenHash(user.get().getId(), tokenHash);

        return ResponseEntity.ok("로그아웃 성공");
    }
}
