package com.example.luckydaybackend.dto;

import com.example.luckydaybackend.model.Clover;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CloverDTO {

    private Long id;
    private String content;
    private String imageUrl;
    private String createdAt;
    private String email;
    private String username;
    private String nickname;
    private String profileImage;

    @JsonProperty("parent_clover_id")
    private Long parentCloverId;

    public CloverDTO(Clover clover, String username, String nickname, String profileImage) {
        this.id = clover.getId();
        this.content = clover.getContent();
        this.imageUrl = clover.getImageUrl();
        this.createdAt = clover.getCreatedAt().toString();
        this.email = clover.getUser().getEmail();
        this.username = username;
        this.nickname = nickname;
        this.profileImage = profileImage;

        this.parentCloverId = clover.getParentClover() != null
                ? clover.getParentClover().getId()
                : null;
    }
}