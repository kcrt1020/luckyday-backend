package com.example.luckydaybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    private String username;
    private String email;
    private String nickname;
    private String profileImage;
}
