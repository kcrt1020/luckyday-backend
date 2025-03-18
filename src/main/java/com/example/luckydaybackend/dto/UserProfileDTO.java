package com.example.luckydaybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private String userId;  // ✅ userId 추가
    private String email;
    private String nickname;
    private String profileImage;
    private String bio;
    private String location;
    private String website;
}
