package com.example.luckydaybackend.auth.controller;

import com.example.luckydaybackend.auth.dto.LoginRequest;
import com.example.luckydaybackend.auth.dto.RegisterRequest;
import com.example.luckydaybackend.auth.model.User;
import com.example.luckydaybackend.auth.repository.UserRepository;
import com.example.luckydaybackend.auth.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        System.out.println("🔥 회원가입 요청: " + request.getUsername() + " / " + request.getEmail());

        // ✅ 유저 중복 검사 (UserRepository에 해당 메서드가 구현되어 있어야 함)
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("❌ 이미 존재하는 사용자명입니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("❌ 이미 존재하는 이메일입니다.");
        }

        // ✅ 새로운 유저 생성
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // 🔒 비밀번호 암호화 저장

        userRepository.save(user);

        return ResponseEntity.ok("✅ 회원가입 성공!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        logger.info("🔥 로그인 요청: " + request.getUsername()); // ✅ 사용자명 출력

        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            logger.warn("⚠️ 로그인 실패 - 잘못된 사용자명 또는 비밀번호: " + request.getUsername());
            return ResponseEntity.badRequest().body("잘못된 사용자명 또는 비밀번호입니다.");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        logger.info("✅ 로그인 성공 - 사용자명: " + user.getUsername() + ", JWT: " + token);
        return ResponseEntity.ok(token);
    }
}
