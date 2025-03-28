package com.example.luckydaybackend.service;

import com.example.luckydaybackend.dto.UserResponseDTO;
import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.model.UserProfile;
import com.example.luckydaybackend.repository.UserProfileRepository;
import com.example.luckydaybackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

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
}