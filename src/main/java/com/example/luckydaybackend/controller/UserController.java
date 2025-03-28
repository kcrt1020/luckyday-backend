package com.example.luckydaybackend.controller;

import com.example.luckydaybackend.auth.JwtUtil;
import com.example.luckydaybackend.dto.UserResponseDTO;
import com.example.luckydaybackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            @RequestHeader("Authorization") String token) {

        String jwt = token.replace("Bearer ", "");
        Long id = jwtUtil.extractId(jwt); // ✅ 토큰에서 id 추출

        UserResponseDTO user = userService.getUserById(id); // ✅ id 기반 조회
        return ResponseEntity.ok(user);
    }
}
