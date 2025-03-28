package com.example.luckydaybackend.service;

import com.example.luckydaybackend.dto.UserResponseDTO;
import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.model.UserProfile;
import com.example.luckydaybackend.repository.UserProfileRepository;
import com.example.luckydaybackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found for user id: " + id));

        return new UserResponseDTO(
                user.getUsername(),
                user.getEmail(),
                profile.getNickname(),
                profile.getProfileImage()
        );
    }

    public List<User> searchUsersByKeyword(String keyword) {
        List<User> byUsername = userRepository.findByUsernameContainingIgnoreCase(keyword);

        List<UserProfile> byNickname = userProfileService.findByNicknameContaining(keyword);
        List<User> byNicknameUsers = byNickname.stream()
                .map(UserProfile::getUser)
                .toList();

        Set<Long> seenUserIds = new HashSet<>();
        List<User> merged = new ArrayList<>();

        Stream.concat(byUsername.stream(), byNicknameUsers.stream()).forEach(user -> {
            if (seenUserIds.add(user.getId())) {
                merged.add(user);
            }
        });

        return merged;
    }
}
