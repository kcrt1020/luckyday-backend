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

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final StorageService storageService;
    private final UserRepository userRepository;

    public Optional<UserProfile> findByUserId(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null ? userProfileRepository.findByUser(user) : Optional.empty();
    }

    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        return UserProfileDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(profile.getNickname())
                .profileImage(profile.getProfileImage())
                .bio(profile.getBio())
                .location(profile.getLocation())
                .website(profile.getWebsite())
                .birthDate(profile.getBirthDate())
                .build();
    }

    public UserProfileDTO uploadProfileImage(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        String fileName = "avatar_" + user.getUsername() + "_" + file.getOriginalFilename();
        String imageUrl = storageService.saveImage(file, fileName);

        profile.setProfileImage(imageUrl);
        userProfileRepository.save(profile);

        return getUserProfile(userId);
    }

    public void updateUsername(Long userId, String newUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setUsername(newUsername);
        userRepository.save(user);
    }

    @Transactional
    public void updateUserProfile(Long userId, UserProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("UserProfile not found"));

        profile.setNickname(dto.getNickname());
        profile.setBio(dto.getBio());
        profile.setLocation(dto.getLocation());
        profile.setWebsite(dto.getWebsite());
        profile.setBirthDate(dto.getBirthDate());

        userProfileRepository.save(profile);
    }

    public UserProfileDTO getUserProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return getUserProfile(user.getId());
    }

    public List<UserProfile> findByNicknameContaining(String keyword) {
        return userProfileRepository.findByNicknameContainingIgnoreCase(keyword);
    }
}
