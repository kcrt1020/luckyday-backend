package com.example.luckydaybackend.repository;

import com.example.luckydaybackend.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    // ✅ 특정 유저의 세션 찾기
    Optional<UserSession> findByUserId(Long userId);

    // ✅ 특정 유저의 특정 토큰 세션 찾기
    Optional<UserSession> findByUserIdAndTokenHash(Long userId, String tokenHash);

    // ✅ 특정 유저의 모든 세션 삭제 (중복 로그인 방지)
    void deleteByUserId(Long userId);

    // ✅ 만료된 세션 삭제
    void deleteByExpiresAtBefore(Instant now);

    void deleteByUserIdAndTokenHash(Long userId, String tokenHash);

    Optional<UserSession> findByTokenHash(String tokenHash);
}