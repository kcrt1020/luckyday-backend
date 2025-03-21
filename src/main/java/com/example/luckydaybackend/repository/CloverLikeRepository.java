package com.example.luckydaybackend.repository;

import com.example.luckydaybackend.model.Clover;
import com.example.luckydaybackend.model.CloverLike;
import com.example.luckydaybackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CloverLikeRepository extends JpaRepository<CloverLike, Long> {
    Optional<CloverLike> findByCloverIdAndUserEmail(Long cloverId, String email);

    long countByCloverId(Long cloverId);

    boolean existsByUserAndClover(User user, Clover clover);

    void deleteByUserAndClover(User user, Clover clover);
}
