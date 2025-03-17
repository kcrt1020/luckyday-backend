package com.example.luckydaybackend.controller;

import com.example.luckydaybackend.auth.UserPrincipal;
import com.example.luckydaybackend.dto.UserProfileDTO;
import com.example.luckydaybackend.model.Clover;
import com.example.luckydaybackend.service.CloverService;
import com.example.luckydaybackend.service.StorageService;
import com.example.luckydaybackend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final StorageService storageService; // ✅ 변수명 수정
    private final UserProfileService userProfileService;
    private final CloverService cloverService; // ✅ 클로버 관련 서비스 추가

    /**
     * 로그인된 사용자의 프로필 가져오기 (이메일 기반)
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getUserProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UserProfileDTO userProfile = userProfileService.getUserProfile(userPrincipal.getEmail()); // ✅ 이메일로 조회
        return ResponseEntity.ok(userProfile);
    }

    /**
     * 로그인된 사용자가 작성한 클로버 목록 가져오기 (이메일 기반)
     */
    @GetMapping("/clovers")
    public ResponseEntity<List<Clover>> getUserClovers(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<Clover> clovers = cloverService.getCloversByEmail(userPrincipal.getEmail()); // ✅ 이메일로 변경
        return ResponseEntity.ok(clovers);
    }

    /**
     * 프로필 이미지 업로드 (이메일 기반)
     */
    @PostMapping("/avatar")
    public ResponseEntity<UserProfileDTO> uploadAvatar(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("profileImage") MultipartFile file) {

        // ✅ 파일 이름을 이메일 기반으로 저장
        String sanitizedEmail = userPrincipal.getEmail().replace("@", "_").replace(".", "_");
        String fileName = "avatar_" + sanitizedEmail + "_" + file.getOriginalFilename();
        String imageUrl = storageService.saveImage(file, fileName); // ✅ 수정

        UserProfileDTO dto = userProfileService.updateProfileImage(userPrincipal.getEmail(), imageUrl); // ✅ 이메일 기반 변경
        return ResponseEntity.ok(dto);
    }
}
