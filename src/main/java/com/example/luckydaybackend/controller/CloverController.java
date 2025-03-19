package com.example.luckydaybackend.controller;

import com.example.luckydaybackend.auth.JwtUtil;
import com.example.luckydaybackend.dto.CloverDTO;
import com.example.luckydaybackend.model.Clover;
import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.model.UserProfile;
import com.example.luckydaybackend.service.CloverService;
import com.example.luckydaybackend.service.UserProfileService;
import com.example.luckydaybackend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clovers")
public class CloverController {
    private final CloverService cloverService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserProfileService userProfileService;

    // ✅ 생성자에서 UserProfileService 추가
    public CloverController(CloverService cloverService, JwtUtil jwtUtil, UserService userService, UserProfileService userProfileService) {
        this.cloverService = cloverService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userProfileService = userProfileService;
    }

    /**
     * 클로버 생성 API
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createClover(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestPart("content") String contentJson,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            // ✅ JWT 토큰 검증
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT 토큰이 필요합니다.");
            }
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);

            // ✅ 이메일로 유저 조회
            User user = userService.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
            }

            // ✅ 트윗 JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            Clover clover = objectMapper.readValue(contentJson, Clover.class);

            if (clover.getContent() == null || clover.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("트윗 내용이 없습니다.");
            }

            // ✅ 이메일 저장 (userId, nickname 저장 X)
            clover.setEmail(email);

            // ✅ 파일 업로드 처리
            if (file != null && !file.isEmpty()) {
                String uploadDir = "uploads/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                clover.setImageUrl("/uploads/" + fileName);
            }

            // ✅ 클로버 저장
            Clover savedClover = cloverService.createClover(clover);
            return ResponseEntity.ok(savedClover);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("트윗 등록 중 오류 발생");
        }
    }

    /**
     * 모든 클로버 조회 API
     */
    @GetMapping
    public ResponseEntity<List<CloverDTO>> getAllClovers() {
        List<Clover> clovers = cloverService.getAllClovers();

        // ✅ 이메일 기반으로 유저 정보 조회 (UserProfile에서 닉네임 가져옴)
        List<CloverDTO> cloverDTOs = clovers.stream().map(clover -> {
            User user = userService.findByEmail(clover.getEmail());
            Optional<UserProfile> userProfile = userProfileService.findByEmail(clover.getEmail()); // ✅ 수정

            String userId = (user != null) ? user.getUserId() : "Unknown";
            String nickname = userProfile.map(UserProfile::getNickname).orElse("Unknown"); // ✅ Optional 사용
            String profileImage = userProfile.map(UserProfile::getProfileImage).orElse("Unknown"); // ✅ Optional 사용

            return new CloverDTO(clover, userId, nickname, profileImage);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(cloverDTOs);
    }

    /**
     * 특정 유저 클로버 조회 API
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CloverDTO>> getCloversByUserId(@PathVariable String userId) {
        // ✅ 유저 아이디(userId)로 유저 이메일(email) 조회
        User user = userService.findByUserId(userId);
        if (user == null) {
            return ResponseEntity.notFound().build(); // 유저가 없으면 404 반환
        }

        String email = user.getEmail();

        // ✅ 유저 이메일로 클로버 조회
        List<Clover> clovers = cloverService.getCloversByEmail(email);

        // ✅ 이메일 기반으로 유저 프로필 조회 (닉네임 가져옴)
        Optional<UserProfile> userProfile = userProfileService.findByEmail(email);
        String nickname = userProfile.map(UserProfile::getNickname).orElse("Unknown");
        String profileImage = userProfile.map(UserProfile::getProfileImage).orElse("Unknown");

        // ✅ DTO 변환
        List<CloverDTO> cloverDTOs = clovers.stream()
                .map(clover -> new CloverDTO(clover, userId, nickname, profileImage))
                .collect(Collectors.toList());

        return ResponseEntity.ok(cloverDTOs);
    }


    /**
     * 특정 클로버 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCloverById(@PathVariable Long id) {
        Clover clover = cloverService.getCloverById(id);
        if (clover == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("트윗을 찾을 수 없습니다.");
        }

        User user = userService.findByEmail(clover.getEmail());
        Optional<UserProfile> userProfile = userProfileService.findByEmail(clover.getEmail()); // ✅ 수정: Optional 사용

        String userId = (user != null) ? user.getUserId() : "Unknown";
        String nickname = userProfile.map(UserProfile::getNickname).orElse("Unknown"); // ✅ Optional 사용
        String profileImage = userProfile.map(UserProfile::getProfileImage).orElse("Unknown"); // ✅ Optional 사용

        return ResponseEntity.ok(new CloverDTO(clover, userId, nickname, profileImage));
    }


    /**
     * 클로버 삭제 API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClover(@PathVariable Long id) {
        Clover clover = cloverService.getCloverById(id);
        if (clover == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("트윗을 찾을 수 없습니다.");
        }

        cloverService.deleteClover(id);
        return ResponseEntity.ok("트윗이 삭제되었습니다.");
    }
}
