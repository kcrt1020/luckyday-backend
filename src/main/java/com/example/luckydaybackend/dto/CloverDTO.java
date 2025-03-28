package com.example.luckydaybackend.dto;

import com.example.luckydaybackend.model.Clover;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CloverDTO {
    private Long id;
    private String content;
    private String imageUrl;
    private String createdAt;
    private String username;
    private String nickname;
    private String profileImage;

    public CloverDTO(Clover clover, String username, String nickname, String profileImage) {
        this.id = clover.getId();
        this.content = clover.getContent();
        this.imageUrl = clover.getImageUrl();
        this.createdAt = clover.getCreatedAt().toString();
        this.username = username;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }
}
