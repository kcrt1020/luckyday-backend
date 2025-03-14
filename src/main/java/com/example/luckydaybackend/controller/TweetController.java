package com.example.luckydaybackend.controller;

import com.example.luckydaybackend.auth.model.User;
import com.example.luckydaybackend.auth.utils.JwtUtil;
import com.example.luckydaybackend.model.Tweet;
import com.example.luckydaybackend.service.TweetService;
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

@RestController
@RequestMapping("/api/tweets")
public class TweetController {
    private final TweetService tweetService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public TweetController(TweetService tweetService, JwtUtil jwtUtil, UserService userService) {
        this.tweetService = tweetService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createTweet(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestPart(value = "content", required = true) String contentJson,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            // ✅ JWT 토큰 검증
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT 토큰이 필요합니다.");
            }
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);

            // ✅ 이메일로 유저네임 조회
            User user = userService.findByEmail(email); // ✅ 이메일로 유저 찾기
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
            }
            String username = user.getUsername(); // ✅ 유저네임 가져오기

            // ✅ 트윗 JSON 파싱 및 예외 처리
            if (contentJson == null || contentJson.isEmpty()) {
                return ResponseEntity.badRequest().body("트윗 내용이 비어 있습니다.");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            Tweet tweet = objectMapper.readValue(contentJson, Tweet.class);

            if (tweet.getContent() == null || tweet.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("트윗 내용이 없습니다.");
            }

            tweet.setEmail(email);
            tweet.setUsername(username);

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

                tweet.setImageUrl("/uploads/" + fileName);
            }

            return ResponseEntity.ok(tweetService.createTweet(tweet));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("트윗 등록 중 오류 발생");
        }
    }


    // 모든 트윗 조회
    @GetMapping
    public List<Tweet> getAllTweets() {
        return tweetService.getAllTweets();
    }

    // 개별 트윗 조회
    @GetMapping("/{id}")
    public Tweet getTweetById(@PathVariable Long id) {
        return tweetService.getTweetById(id);
    }

    // 트윗 삭제
    @DeleteMapping("/{id}")
    public void deleteTweet(@PathVariable Long id) {
        tweetService.deleteTweet(id);
    }
}
