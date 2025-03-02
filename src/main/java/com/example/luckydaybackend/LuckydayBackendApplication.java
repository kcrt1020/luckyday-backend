package com.example.luckydaybackend;

import com.example.luckydaybackend.config.SecurityConfig; // ✅ SecurityConfig 직접 가져오기!
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SecurityConfig.class)  // ✅ SecurityConfig를 강제 로드
public class LuckydayBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(LuckydayBackendApplication.class, args);
    }
}
