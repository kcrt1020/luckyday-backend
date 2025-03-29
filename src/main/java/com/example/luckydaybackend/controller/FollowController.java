package com.example.luckydaybackend.controller;

import com.example.luckydaybackend.auth.UserPrincipal;
import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.service.FollowService;
import com.example.luckydaybackend.service.NotificationService;
import com.example.luckydaybackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowService followService;
    private final UserService userService;
    private final NotificationService notificationService;

    // 팔로우 상태 확인
    @GetMapping("/status/{targetUserId}")
    public ResponseEntity<?> checkFollowStatus(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @PathVariable String targetUserId) {
        // targetUserId로 유저 조회
        User targetUser = followService.getUserByUserId(targetUserId);

        // 팔로우 상태 확인
        boolean isFollowing = followService.isFollowing(userPrincipal.getEmail(), targetUser);

        return ResponseEntity.ok().body(Map.of("isFollowing", isFollowing));
    }

    // 팔로우 하기
    @PostMapping("/{targetUserId}")
    public ResponseEntity<?> follow(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                    @PathVariable String targetUserId) {
        // targetUserId로 유저 조회
        User targetUser = followService.getUserByUserId(targetUserId);

        // 팔로우 하기
        followService.follow(userPrincipal.getEmail(), targetUser);

        // 내 정보 가져오기 (팔로우 건 사람)
        User sender = userService.findById(userPrincipal.getId());

        // 자기 자신 팔로우가 아닌 경우에만 알림
        if (!sender.getId().equals(targetUser.getId())) {
            notificationService.sendNotification(
                    targetUser.getId(),              // 알림 받는 사람
                    sender.getId(),                  // 보낸 사람
                    "FOLLOW",                        // 알림 타입
                    sender.getId(),                  // 타겟 ID (보낸 사람의 ID → 프로필 클릭용)
                    "/profile/" + sender.getUsername() // 공개용 userId로 URL 구성
            );
        }


        return ResponseEntity.ok().body(Map.of("message", "팔로우 완료"));
    }


    // 언팔로우 하기
    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<?> unfollow(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                      @PathVariable String targetUserId) {
        // targetUserId로 유저 조회
        User targetUser = followService.getUserByUserId(targetUserId);

        // 언팔로우 하기
        followService.unfollow(userPrincipal.getEmail(), targetUser);

        return ResponseEntity.ok().body(Map.of("message", "언팔로우 완료"));
    }

    // 팔로잉 목록 조회
    @GetMapping("/following/{targetUserId}")
    public ResponseEntity<?> getFollowingList(@PathVariable String targetUserId) {
        User user = followService.getUserByUserId(targetUserId);
        System.out.println("📌 user = " + user);
        List<User> followingList = followService.getFollowingList(user);
        System.out.println("📌 followingList = " + followingList);
        return ResponseEntity.ok().body(followingList);
    }

    // 팔로워 목록 조회
    @GetMapping("/followers/{targetUserId}")
    public ResponseEntity<?> getFollowersList(@PathVariable String targetUserId) {
        User user = followService.getUserByUserId(targetUserId);
        List<User> followersList = followService.getFollowersList(user);
        return ResponseEntity.ok().body(followersList);
    }


    // 팔로잉 수 조회
    @GetMapping("/following/count")
    public ResponseEntity<?> getFollowingCount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User fromUser = followService.getUserById(userPrincipal.getId());
        long followingCount = followService.getFollowingCount(fromUser);
        return ResponseEntity.ok().body(Map.of("followingCount", followingCount));
    }

    // 팔로워 수 조회
    @GetMapping("/followers/count")
    public ResponseEntity<?> getFollowersCount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User toUser = followService.getUserById(userPrincipal.getId());
        long followersCount = followService.getFollowersCount(toUser);
        return ResponseEntity.ok().body(Map.of("followersCount", followersCount));
    }
}
