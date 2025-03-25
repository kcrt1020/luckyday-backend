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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
     * 로그인된 사용자의 프로필 가져오기
     */
    @GetMapping("/me")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            System.out.println("🚨 @AuthenticationPrincipal이 null입니다. SecurityContext에서 가져오는 방법 시도...");

            // ✅ SecurityContext에서 직접 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
                System.out.println("🚨 SecurityContextHolder에서도 UserPrincipal을 찾을 수 없음!");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized access: UserPrincipal is null");
            }

            userPrincipal = (UserPrincipal) authentication.getPrincipal();
            System.out.println("✅ SecurityContextHolder에서 UserPrincipal 가져옴: " + userPrincipal.getEmail());
        }

        String email = userPrincipal.getEmail();

        // ✅ userProfileService에서 userId 포함된 정보 가져오기
        UserProfileDTO userProfile = userProfileService.getUserProfile(email);

        return ResponseEntity.ok(userProfile);
    }


    /**
     * 로그인된 사용자가 작성한 클로버 목록 가져오기
     */
    @GetMapping("/clovers")
    public ResponseEntity<List<Clover>> getUserClovers(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            System.out.println("🚨 userPrincipal이 null입니다!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String email = userPrincipal.getEmail();
        System.out.println("✅ 클로버 조회 요청 - 이메일: " + email);

        List<Clover> clovers = cloverService.getCloversByEmail(email);

        if (clovers.isEmpty()) {
            System.out.println("⚠️ 클로버 데이터가 없음!");
        } else {
            System.out.println("✅ 클로버 데이터 반환: " + clovers);
        }

        return ResponseEntity.ok(clovers);
    }


    /**
     * 프로필 이미지 업로드 (이메일 기반)
     */
    @PostMapping("/avatar")
    public ResponseEntity<UserProfileDTO> uploadAvatar(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("profileImage") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        String sanitizedEmail = userPrincipal.getEmail().replace("@", "_").replace(".", "_");
        String originalFileName = file.getOriginalFilename();

        System.out.println("Uploading file: " + originalFileName); // 로그 추가

        if (originalFileName == null) {
            return ResponseEntity.badRequest().body(null);
        }

        String fileName = "avatar_" + sanitizedEmail + "_" + originalFileName;
        String imageUrl = storageService.saveImage(file, fileName);

        System.out.println("File saved as: " + imageUrl); // 로그 추가

        UserProfileDTO dto = userProfileService.updateProfileImage(userPrincipal.getEmail(), imageUrl);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUserProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UserProfileDTO profileDTO
    ) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized access");
        }

        String email = userPrincipal.getEmail();

        // 1. user_profile 테이블 업데이트
        userProfileService.updateUserProfile(email, profileDTO);

        // 2. users 테이블에서 user_id (표시용 ID) 업데이트
        userProfileService.updateUserId(email, profileDTO.getUserId());

        return ResponseEntity.ok(profileDTO);
    }

}