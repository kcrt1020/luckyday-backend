package com.example.luckydaybackend.service;

import com.example.luckydaybackend.dto.UserResponseDTO;
import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.model.UserProfile;
import com.example.luckydaybackend.repository.UserProfileRepository;
import com.example.luckydaybackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileService userProfileService;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null); // ✅ 유저가 없으면 null 반환
    }

    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElse(null); // ✅ 유저가 없으면 null 반환
    }

    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        UserProfile profile = userProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found for email: " + email));

        return new UserResponseDTO(user.getUserId(), user.getEmail(), profile.getNickname(), profile.getProfileImage());
    }

    public List<User> searchUsersByKeyword(String keyword) {
        List<User> byUserId = userRepository.findByUserIdContainingIgnoreCase(keyword);

        List<UserProfile> byNickname = userProfileService.findByNicknameContaining(keyword);
        List<User> byNicknameUsers = byNickname.stream()
                .map(profile -> profile.getUser().getEmail())
                .map(userRepository::findByEmail)
                .flatMap(Optional::stream)
                .toList();


        // userId 검색 + nickname 검색 결과 합치고 중복 제거
        Set<String> seenEmails = new HashSet<>();
        List<User> merged = new ArrayList<>();

        Stream.concat(byUserId.stream(), byNicknameUsers.stream()).forEach(user -> {
            if (seenEmails.add(user.getEmail())) {
                merged.add(user);
            }
        });

        return merged;
    }

}