package com.example.luckydaybackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileDTO {
    private String userId;  // ✅ userId 추가
    private String email;
    private String nickname;
    private String profileImage;
    private String bio;
    private String location;
    private String website;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
}
