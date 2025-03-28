package com.example.luckydaybackend.repository;

import com.example.luckydaybackend.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    // ✅ 이메일을 기준으로 유저 프로필 조회 (Optional 반환)
    Optional<UserProfile> findByUser_Email(String email);

}