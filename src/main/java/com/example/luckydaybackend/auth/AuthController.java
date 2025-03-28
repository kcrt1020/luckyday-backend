package com.example.luckydaybackend.auth;

import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.model.UserProfile;
import com.example.luckydaybackend.repository.UserProfileRepository;
import com.example.luckydaybackend.repository.UserRepository;
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
    private final UserProfileRepository userProfileRepository;

    public AuthController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserSessionRepository userSessionRepository, UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userSessionRepository = userSessionRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        System.out.println("🔥 회원가입 요청: " + request.getUsername() + " / " + request.getEmail());

        // ✅ 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("❌ 이미 존재하는 이메일입니다.");
        }

        // ✅ 아이디 중복 검사
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("❌ 이미 존재하는 아이디입니다.");
        }

        // ✅ 새로운 유저 생성
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // 🔒 비밀번호 암호화

        userRepository.save(user);
        System.out.println("✨ 신규 유저 생성: " + user.getEmail());

        // ✅ `user_profile` 생성 (이제 email 필드 X, 대신 user 연결)
        UserProfile userProfile = new UserProfile();
        userProfile.setUser(user); // ✅ user 객체 설정
        userProfile.setNickname(request.getNickname());
        userProfile.setProfileImage(null);
        userProfile.setBio(null);
        userProfile.setLocation(null);
        userProfile.setWebsite(null);
        userProfile.setBirthDate(null);

        userProfileRepository.save(userProfile);
        System.out.println("🎉 유저 프로필 생성 완료: " + userProfile.getNickname());

        return ResponseEntity.ok("✅ 회원가입 성공!");
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
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        // ✅ 리프레시 토큰을 저장
        UserSession session = new UserSession();
        session.setUserId(user.getId());
        session.setTokenHash(DigestUtils.sha256Hex(refreshToken));
        session.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        userSessionRepository.save(session);

        logger.info("✅ 로그인 성공 - 이메일: " + user.getEmail());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("accessToken", jwtUtil.generateAccessToken(user.getId(), user.getEmail()), "refreshToken", refreshToken));
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

        Long id = jwtUtil.extractId(refreshToken);
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "유효하지 않은 리프레시 토큰"));
        }

        Optional<User> user = userRepository.findById(id);
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

        String newAccessToken = jwtUtil.generateAccessToken(user.get().getId(), user.get().getEmail());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("accessToken", newAccessToken));
    }


    // ✅ 로그아웃 API (리프레시 토큰 삭제)
    @PostMapping("/logout")
    @Transactional // ✅ 트랜잭션 추가
    public ResponseEntity<String> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        Long id = jwtUtil.extractId(refreshToken);

        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
        }

        String tokenHash = jwtUtil.hashToken(refreshToken);
        userSessionRepository.deleteByUserIdAndTokenHash(user.get().getId(), tokenHash);

        return ResponseEntity.ok("로그아웃 성공");
    }
}
