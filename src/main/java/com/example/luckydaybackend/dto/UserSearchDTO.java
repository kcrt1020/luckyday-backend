package com.example.luckydaybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDTO {
    private String username;
    private Profile profile;

    @Getter
    @AllArgsConstructor
    public static class Profile {
        private String nickname;
        private String profileImage;
        private String bio;
    }
}
