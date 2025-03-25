package com.example.luckydaybackend.controller;

import com.example.luckydaybackend.auth.UserPrincipal;
import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.service.FollowService;
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
    @GetMapping("/following")
    public ResponseEntity<?> getFollowingList(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User fromUser = followService.getUserByUserId(userPrincipal.getEmail());
        List<User> followingList = followService.getFollowingList(fromUser);
        return ResponseEntity.ok().body(followingList);
    }

    // 팔로워 목록 조회
    @GetMapping("/followers")
    public ResponseEntity<?> getFollowersList(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User toUser = followService.getUserByUserId(userPrincipal.getEmail());
        List<User> followersList = followService.getFollowersList(toUser);
        return ResponseEntity.ok().body(followersList);
    }

    // 팔로잉 수 조회
    @GetMapping("/following/count")
    public ResponseEntity<?> getFollowingCount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User fromUser = followService.getUserByUserId(userPrincipal.getEmail());
        long followingCount = followService.getFollowingCount(fromUser);
        return ResponseEntity.ok().body(Map.of("followingCount", followingCount));
    }

    // 팔로워 수 조회
    @GetMapping("/followers/count")
    public ResponseEntity<?> getFollowersCount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User toUser = followService.getUserByUserId(userPrincipal.getEmail());
        long followersCount = followService.getFollowersCount(toUser);
        return ResponseEntity.ok().body(Map.of("followersCount", followersCount));
    }
}
