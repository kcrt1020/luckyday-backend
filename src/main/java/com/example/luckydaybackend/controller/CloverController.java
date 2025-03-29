package com.example.luckydaybackend.controller;

import com.example.luckydaybackend.auth.JwtUtil;
import com.example.luckydaybackend.auth.UserPrincipal;
import com.example.luckydaybackend.dto.CloverDTO;
import com.example.luckydaybackend.model.Clover;
import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.model.UserProfile;
import com.example.luckydaybackend.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clovers")
@RequiredArgsConstructor
public class CloverController {

    private final CloverService cloverService;
    private final UserService userService;
    private final StorageService storageService;
    private final NotificationService notificationService;

    /**
     * 클로버 생성 API
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createClover(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("content") String contentJson,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            if (userPrincipal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            Long userId = userPrincipal.getId();
            User user = userService.findById(userId);
            if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");

            ObjectMapper objectMapper = new ObjectMapper();
            Clover clover = objectMapper.readValue(contentJson, Clover.class);

            if (clover.getContent() == null || clover.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("내용이 없습니다.");
            }

            clover.setUser(user);

            // 🔥 답글일 경우 부모 클로버 설정
            if (clover.getParentClover() != null && clover.getParentClover().getId() != null) {
                Clover parent = cloverService.findById(clover.getParentClover().getId());
                if (parent == null) {
                    return ResponseEntity.badRequest().body("존재하지 않는 부모 클로버입니다.");
                }
                clover.setParentClover(parent);

                // 🔔 부모 작성자에게 알림 보내기
                if (!parent.getUser().getId().equals(userId)) {
                    notificationService.sendNotification(
                            parent.getUser().getId(),   // receiverId
                            user.getId(),               // senderId
                            "COMMENT",                  // type
                            parent.getId(),             // targetId (댓글 달린 부모 클로버 ID)
                            "/clovers/" + parent.getId()// url
                    );
                }
            }

            // 📸 이미지 처리
            if (file != null && !file.isEmpty()) {
                String fileName = "clover_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                String imageUrl = storageService.saveImage(file, fileName);
                clover.setImageUrl(imageUrl);
            }

            Clover saved = cloverService.createClover(clover);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("클로버 등록 중 오류 발생");
        }
    }


    /**
     * 모든 클로버 조회 API
     */
    @GetMapping
    public ResponseEntity<List<CloverDTO>> getAllClovers() {
        List<Clover> clovers = cloverService.getAllClovers();

        List<CloverDTO> dtos = clovers.stream().map(clover -> {
            User user = clover.getUser();
            UserProfile profile = user.getProfile();

            return new CloverDTO(
                    clover,
                    user.getUsername(),
                    profile != null ? profile.getNickname() : "Unknown",
                    profile != null ? profile.getProfileImage() : null
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * 특정 유저 클로버 조회 API
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<CloverDTO>> getCloversByUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user == null) return ResponseEntity.notFound().build();

        List<Clover> clovers = cloverService.getCloversByUserId(user.getId());
        UserProfile profile = user.getProfile();

        List<CloverDTO> dtos = clovers.stream().map(clover -> new CloverDTO(
                clover,
                user.getUsername(),
                profile != null ? profile.getNickname() : "Unknown",
                profile != null ? profile.getProfileImage() : null
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * 특정 클로버 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCloverById(@PathVariable Long id) {
        Clover clover = cloverService.getCloverById(id);
        if (clover == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("트윗을 찾을 수 없습니다.");

        User user = clover.getUser();
        UserProfile profile = user.getProfile();

        return ResponseEntity.ok(new CloverDTO(
                clover,
                user.getUsername(),
                profile != null ? profile.getNickname() : "Unknown",
                profile != null ? profile.getProfileImage() : null
        ));
    }

    /**
     * 클로버 삭제 API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClover(@PathVariable Long id) {
        Clover clover = cloverService.getCloverById(id);
        if (clover == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("트윗을 찾을 수 없습니다.");
        }

        cloverService.deleteClover(id);
        return ResponseEntity.ok(Map.of("message", "트윗이 삭제되었습니다."));
    }

    /**
     * 특정 클로버의 댓글 조회 API
     */
    @GetMapping("/replies/{parentId}")
    public ResponseEntity<List<CloverDTO>> getReplies(@PathVariable Long parentId) {
        List<Clover> replies = cloverService.getRepliesByParentId(parentId);

        List<CloverDTO> dtos = replies.stream().map(reply -> {
            User user = reply.getUser();
            UserProfile profile = user.getProfile();

            return new CloverDTO(
                    reply,
                    user.getUsername(),
                    profile != null ? profile.getNickname() : "Unknown",
                    profile != null ? profile.getProfileImage() : null
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
