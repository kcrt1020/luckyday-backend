package com.example.luckydaybackend.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {
    private String userId;
    private String email;
    private String password;
    private String nickname;
}
