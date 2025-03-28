package com.example.luckydaybackend.controller;

import com.example.luckydaybackend.auth.UserPrincipal;
import com.example.luckydaybackend.dto.UserProfileDTO;
import com.example.luckydaybackend.model.Clover;
import com.example.luckydaybackend.service.CloverService;
import com.example.luckydaybackend.service.StorageService;
import com.example.luckydaybackend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final StorageService storageService;
    private final UserProfileService userProfileService;
    private final CloverService cloverService;

    /**
     * 로그인된 사용자의 프로필 가져오기
     */
    @GetMapping("/me")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized access: UserPrincipal is null");
        }

        Long userId = userPrincipal.getId();
        UserProfileDTO userProfile = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    /**
     * 로그인된 사용자가 작성한 클로버 목록 가져오기
     */
    @GetMapping("/clovers")
    public ResponseEntity<List<Clover>> getUserClovers(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        Long userId = userPrincipal.getId();
        List<Clover> clovers = cloverService.getCloversByUserId(userId);
        return ResponseEntity.ok(clovers);
    }

    /**
     * 프로필 이미지 업로드
     */
    @PostMapping("/avatar")
    public ResponseEntity<UserProfileDTO> uploadAvatar(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("profileImage") MultipartFile file) {

        if (userPrincipal == null || file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Long userId = userPrincipal.getId();
        UserProfileDTO dto = userProfileService.uploadProfileImage(userId, file);
        return ResponseEntity.ok(dto);
    }

    /**
     * 프로필 정보 업데이트
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateUserProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UserProfileDTO profileDTO) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized access");
        }

        Long userId = userPrincipal.getId();

        userProfileService.updateUserProfile(userId, profileDTO);
        userProfileService.updateUsername(userId, profileDTO.getUsername());

        return ResponseEntity.ok(profileDTO);
    }

    /**
     * 다른 사용자의 프로필 조회 (username 기반)
     */
    @GetMapping("/{username}")
    public ResponseEntity<?> getOtherUserProfile(@PathVariable String username) {
        UserProfileDTO profile = userProfileService.getUserProfileByUsername(username);

        if (profile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.ok(profile);
    }
}
