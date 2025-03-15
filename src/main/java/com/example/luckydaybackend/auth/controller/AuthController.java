package com.example.luckydaybackend.auth.controller;

import com.example.luckydaybackend.auth.dto.LoginRequest;
import com.example.luckydaybackend.auth.dto.RegisterRequest;
import com.example.luckydaybackend.auth.model.User;
import com.example.luckydaybackend.auth.repository.UserRepository;
import com.example.luckydaybackend.auth.utils.JwtUtil;
import com.example.luckydaybackend.model.UserSession;
import com.example.luckydaybackend.repository.UserSessionRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        System.out.println("🔥 회원가입 요청: " + request.getUsername() + " / " + request.getEmail());

        // ✅ 이메일 중복 검사 (이메일이 이미 존재하면 회원가입 불가)
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("❌ 이미 존재하는 이메일입니다.");
        }

        // ✅ 새로운 유저 생성
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // 🔒 비밀번호 암호화 저장

        userRepository.save(user);
        System.out.println("✨ 신규 유저 생성: " + user.getEmail());

        return ResponseEntity.ok("✅ 회원가입 성공!");
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        logger.info("🔥 로그인 요청 - 이메일: " + request.getEmail());

        // ✅ 이메일로 유저 찾기 (Optional 사용)
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            logger.warn("⚠️ 로그인 실패 - 존재하지 않는 이메일: " + request.getEmail());
            return ResponseEntity.badRequest().body("잘못된 이메일 또는 비밀번호입니다.");
        }

        User user = optionalUser.get();

        // ✅ 비밀번호 검증 (null 체크 추가)
        if (request.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            logger.warn("⚠️ 로그인 실패 - 잘못된 이메일 또는 비밀번호: " + request.getEmail());
            return ResponseEntity.badRequest().body("잘못된 이메일 또는 비밀번호입니다.");
        }

        // ✅ 기존 로그인 세션 삭제 (중복 로그인 방지)
        userSessionRepository.deleteByUserId(user.getId());

        // ✅ JWT 토큰 생성
        String token = jwtUtil.generateToken(user.getEmail());
        String tokenHash = DigestUtils.sha256Hex(token); // 해싱하여 저장

        // ✅ 새 세션 저장
        UserSession session = new UserSession();
        session.setUserId(user.getId());
        session.setTokenHash(tokenHash);
        session.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS)); // 7일 후 만료

        userSessionRepository.save(session);

        logger.info("✅ 로그인 성공 - 이메일: " + user.getEmail() + ", JWT 저장 완료");
        return ResponseEntity.ok(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        String token = authHeader.substring(7);

        // ✅ 토큰 유효성 검사
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }

        // ✅ JWT에서 이메일 추출
        String email = jwtUtil.extractEmail(token);

        // ✅ 이메일로 유저 조회
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
        }

        // ✅ 토큰 해싱 후 세션 삭제
        String tokenHash = DigestUtils.sha256Hex(token);
        Optional<UserSession> session = userSessionRepository.findByUserIdAndTokenHash(user.get().getId(), tokenHash);

        if (session.isPresent()) {
            userSessionRepository.delete(session.get());
            return ResponseEntity.ok("로그아웃 성공");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 세션을 찾을 수 없거나 이미 로그아웃되었습니다.");
        }
    }




}
