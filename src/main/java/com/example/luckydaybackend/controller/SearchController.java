package com.example.luckydaybackend.controller;

import com.example.luckydaybackend.dto.CloverDTO;
import com.example.luckydaybackend.dto.UserSearchDTO;
import com.example.luckydaybackend.model.Clover;
import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.model.UserProfile;
import com.example.luckydaybackend.service.CloverService;
import com.example.luckydaybackend.service.UserProfileService;
import com.example.luckydaybackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final CloverService cloverService;
    private final UserService userService;
    private final UserProfileService userProfileService;

    public SearchController(CloverService cloverService,
                            UserService userService,
                            UserProfileService userProfileService) {
        this.cloverService = cloverService;
        this.userService = userService;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/clovers/{keyword}")
    public ResponseEntity<List<CloverDTO>> searchClovers(@PathVariable String keyword) {
        List<Clover> clovers = cloverService.searchCloversByKeyword(keyword);
        List<CloverDTO> result = clovers.stream().map(clover -> {
            User user = userService.findByEmail(clover.getEmail());
            Optional<UserProfile> profileOpt = userProfileService.findByEmail(clover.getEmail());

            String userId = (user != null) ? user.getUserId() : "Unknown";
            String nickname = profileOpt.map(UserProfile::getNickname).orElse("Unknown");
            String profileImage = profileOpt.map(UserProfile::getProfileImage).orElse("Unknown");

            return new CloverDTO(clover, userId, nickname, profileImage);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }



    @GetMapping("/users/{keyword}")
    public ResponseEntity<List<UserSearchDTO>> searchUsers(@PathVariable String keyword) {
        List<User> users = userService.searchUsersByKeyword(keyword);

        List<UserSearchDTO> result = users.stream().map(user -> {
            Optional<UserProfile> profileOpt = userProfileService.findByEmail(user.getEmail());

            String nickname = profileOpt.map(UserProfile::getNickname).orElse("Unknown");
            String profileImage = profileOpt.map(UserProfile::getProfileImage).orElse("Unknown");

            return new UserSearchDTO(user.getUserId(), nickname, profileImage);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

}
