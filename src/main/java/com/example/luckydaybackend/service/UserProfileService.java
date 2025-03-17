package com.example.luckydaybackend.service;

import com.example.luckydaybackend.dto.UserProfileDTO;
import com.example.luckydaybackend.model.UserProfile;
import com.example.luckydaybackend.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final StorageService storageService; // ✅ 올바른 서비스 사용

    /**
     * 이메일 기반 유저 프로필 조회 (Optional)
     */
    public Optional<UserProfile> findByEmail(String email) {
        return userProfileRepository.findByEmail(email);
    }

    /**
     * 이메일 기반 유저 프로필 조회
     */
    public UserProfileDTO getUserProfile(String email) {
        UserProfile profile = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        return new UserProfileDTO(
                profile.getEmail(),
                profile.getNickname(),
                profile.getProfileImage(),
                profile.getBio(),
                profile.getLocation(),
                profile.getWebsite()
        );
    }

    /**
     * 프로필 이미지 업로드 (이메일 기반)
     */
    public UserProfileDTO uploadProfileImage(String email, MultipartFile file) {
        UserProfile profile = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        // 저장할 파일 이름 (파일 중복 방지)
        String fileName = "avatar_" + email.replace("@", "_") + "_" + file.getOriginalFilename();

        // 로컬 저장소에 이미지 저장
        String imageUrl = storageService.saveImage(file, fileName);

        // 프로필 이미지 업데이트 및 DTO 반환
        return updateProfileImage(email, imageUrl);
    }

    /**
     * 프로필 이미지 업데이트 (이메일 기반)
     */
    public UserProfileDTO updateProfileImage(String email, String imageUrl) {
        UserProfile profile = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        profile.setProfileImage(imageUrl);
        userProfileRepository.save(profile);

        return new UserProfileDTO(
                profile.getEmail(),
                profile.getNickname(),
                profile.getProfileImage(),
                profile.getBio(),
                profile.getLocation(),
                profile.getWebsite()
        );
    }
}
