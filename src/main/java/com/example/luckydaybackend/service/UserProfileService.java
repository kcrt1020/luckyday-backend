package com.example.luckydaybackend.service;

import com.example.luckydaybackend.dto.UserProfileDTO;
import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.model.UserProfile;
import com.example.luckydaybackend.repository.UserProfileRepository;
import com.example.luckydaybackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final StorageService storageService; // ✅ 올바른 서비스 사용
    private final UserRepository userRepository;

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

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("유저를 찾을 수 없습니다: " + email);
        }

        User user = optionalUser.get();  // ✅ userId가 User 테이블에 있으므로 가져옴

        return new UserProfileDTO(
                user.getUserId(),
                profile.getEmail(),
                profile.getNickname(),
                profile.getProfileImage(),
                profile.getBio(),
                profile.getLocation(),
                profile.getWebsite(),
                profile.getBirthDate()
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

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("유저를 찾을 수 없습니다: " + email);
        }
        User user = optionalUser.get();  // ✅ userId가 User 테이블에 있으므로 가져옴


        profile.setProfileImage(imageUrl);
        userProfileRepository.save(profile);

        return new UserProfileDTO(
                user.getUserId(),
                profile.getEmail(),
                profile.getNickname(),
                profile.getProfileImage(),
                profile.getBio(),
                profile.getLocation(),
                profile.getWebsite(),
                profile.getBirthDate()
        );
    }

    @Transactional
    public void updateUserId(String email, String newUserId) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            user.get().setUserId(newUserId);
            userRepository.save(user.get());
        } else {
            throw new RuntimeException("User not found with email: " + email);
        }
    }

    @Transactional
    public void updateUserProfile(String email, UserProfileDTO dto) {
        Optional<UserProfile> optionalProfile = userProfileRepository.findByEmail(email);

        if (optionalProfile.isPresent()) {
            UserProfile profile = optionalProfile.get();

            profile.setNickname(dto.getNickname());
            profile.setBio(dto.getBio());
            profile.setLocation(dto.getLocation());
            profile.setWebsite(dto.getWebsite());
            profile.setBirthDate(dto.getBirthDate());

            userProfileRepository.save(profile);
        } else {
            throw new RuntimeException("UserProfile not found for email: " + email);
        }
    }

    public UserProfileDTO getUserProfileByUserId(String userId) {
        // userId로 users 테이블에서 이메일 or 사용자 정보 가져오기
        // 그 정보를 바탕으로 user_profile 테이블에서 상세 프로필 조회
        Optional<User> optionalUser = userRepository.findByUserId(userId);
        if (optionalUser.isEmpty()) return null;

        String email = optionalUser.get().getEmail();
        return getUserProfile(email); // 기존 이메일 기반 조회 재사용
    }



}
