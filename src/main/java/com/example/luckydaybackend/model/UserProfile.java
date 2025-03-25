package com.example.luckydaybackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "user_profile")
@Getter @Setter
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // ✅ 유저 ID 대신 이메일 저장

    private String nickname;
    private String profileImage;
    private String bio;
    private String location;
    private String website;

    private LocalDate birthDate;

    @Column(name = "created_at", updatable = false, insertable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", insertable = false) // ✅ 수정 시 자동 갱신
    private Timestamp updatedAt;
}