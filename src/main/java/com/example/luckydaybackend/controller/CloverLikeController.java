package com.example.luckydaybackend.controller;

import com.example.luckydaybackend.auth.JwtUtil;
import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.repository.UserRepository;
import com.example.luckydaybackend.service.CloverLikeService;
import com.example.luckydaybackend.service.CloverService;
import com.example.luckydaybackend.service.NotificationService;
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
    private final CloverService cloverService;
    private final JwtUtil jwtUtil;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // ✅ JWT에서 ID 추출 유틸
    private Long extractUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("JWT 토큰이 필요합니다.");
        }
        String token = authHeader.substring(7);
        return jwtUtil.extractId(token);
    }

    /**
     * 클로버 좋아요 등록
     */
    @PostMapping("/{cloverId}/like")
    public ResponseEntity<?> likeClover(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long cloverId
    ) {
        Long id = extractUserIdFromHeader(authHeader);

        cloverLikeService.likeClover(cloverId, id);

        Long receiverId = cloverService.getAuthorIdByCloverId(cloverId)
                ;
        if (!receiverId.equals(id)) {
            notificationService.sendNotification(
                    receiverId,            // 알림 받을 유저
                    id,                    // 알림 보낸 유저 (좋아요 누른 사람)
                    "LIKE",                // 알림 타입
                    cloverId,              // 타겟 ID (해당 클로버)
                    "/clovers/" + cloverId // URL
            );
        }

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
        Long userId = extractUserIdFromHeader(authHeader);
        cloverLikeService.unlikeClover(cloverId, userId);
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
        Long userId = extractUserIdFromHeader(authHeader);
        boolean liked = cloverLikeService.hasUserLiked(cloverId, userId);
        return ResponseEntity.ok(liked);
    }
}