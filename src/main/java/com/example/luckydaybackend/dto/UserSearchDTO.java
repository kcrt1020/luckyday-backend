package com.example.luckydaybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class UserSearchDTO {
    private String username;
    private String nickname;
    private String profileImage;
}
