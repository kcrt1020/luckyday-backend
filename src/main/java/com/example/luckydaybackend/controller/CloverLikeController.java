package com.example.luckydaybackend.controller;

import com.example.luckydaybackend.auth.JwtUtil;
import com.example.luckydaybackend.service.CloverLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cloverLike")
@RequiredArgsConstructor
public class CloverLikeController {

    private final CloverLikeService cloverLikeService;
    private final JwtUtil jwtUtil;

    /**
     * 클로버 좋아요 등록
     */
    @PostMapping("/{cloverId}/like")
    public ResponseEntity<?> likeClover(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long cloverId
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("JWT 토큰이 필요합니다.");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        cloverLikeService.likeClover(cloverId, email);
        return ResponseEntity.ok(Map.of("message", "좋아요 성공!"));

    }

    /**
     * 클로버 좋아요 취소
     */
    @DeleteMapping("/{cloverId}/like")
    public ResponseEntity<?> unlikeClover(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long cloverId
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("JWT 토큰이 필요합니다.");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        cloverLikeService.unlikeClover(cloverId, email);
        return ResponseEntity.ok(Map.of("message", "좋아요 취소 성공!"));
    }

    /**
     * 클로버 좋아요 수 조회
     */
    @GetMapping("/{cloverId}/likes")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long cloverId) {
        Long count = cloverLikeService.getLikeCount(cloverId);
        return ResponseEntity.ok(count);
    }

    /**
     * 클로버 좋아요 여부 확인 (현재 로그인한 사용자 기준)
     */
    @GetMapping("/{cloverId}/liked")
    public ResponseEntity<Boolean> hasLiked(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long cloverId
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(false);
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        boolean liked = cloverLikeService.hasUserLiked(cloverId, email);
        return ResponseEntity.ok(liked);
    }
}
