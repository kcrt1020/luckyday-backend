package com.example.luckydaybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor  // 기본 생성자 추가 (빈 객체 생성 가능)
public class UserProfileDTO {
    private String email;
    private String nickname;
    private String profileImage;
    private String bio;
    private String location;
    private String website;
}