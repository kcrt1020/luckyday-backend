package com.example.luckydaybackend.controller;

import com.example.luckydaybackend.dto.CloverDTO;
import com.example.luckydaybackend.dto.UserSearchDTO;
import com.example.luckydaybackend.model.Clover;
import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.model.UserProfile;
import com.example.luckydaybackend.service.CloverService;
import com.example.luckydaybackend.service.UserProfileService;
import com.example.luckydaybackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final CloverService cloverService;
    private final UserService userService;
    private final UserProfileService userProfileService;

    /**
     * 키워드로 클로버 검색
     */
    @GetMapping("/clovers/{keyword}")
    public ResponseEntity<List<CloverDTO>> searchClovers(@PathVariable String keyword) {
        List<Clover> clovers = cloverService.searchCloversByKeyword(keyword);

        List<CloverDTO> result = clovers.stream().map(clover -> {
            User user = clover.getUser();
            UserProfile profile = user.getProfile();

            String username = user.getUsername();
            String nickname = (profile != null) ? profile.getNickname() : "Unknown";
            String profileImage = (profile != null) ? profile.getProfileImage() : null;

            return new CloverDTO(clover, username, nickname, profileImage);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * 키워드로 유저 검색
     */
    @GetMapping("/users/{keyword}")
    public ResponseEntity<List<UserSearchDTO>> searchUsers(@PathVariable String keyword) {
        List<User> users = userService.searchUsersByKeyword(keyword);

        List<UserSearchDTO> result = users.stream().map(user -> {
            UserProfile profile = user.getProfile();

            String nickname = (profile != null) ? profile.getNickname() : "Unknown";
            String profileImage = (profile != null) ? profile.getProfileImage() : null;
            String bio = (profile != null) ? profile.getBio() : null;

            return new UserSearchDTO(
                    user.getUsername(),
                    new UserSearchDTO.Profile(nickname, profileImage, bio)
            );
        }).collect(Collectors.toList());


        return ResponseEntity.ok(result);
    }
}
