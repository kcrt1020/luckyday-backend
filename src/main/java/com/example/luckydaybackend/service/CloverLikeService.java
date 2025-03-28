package com.example.luckydaybackend.service;

import com.example.luckydaybackend.model.Clover;
import com.example.luckydaybackend.model.CloverLike;
import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.repository.CloverLikeRepository;
import com.example.luckydaybackend.repository.CloverRepository;
import com.example.luckydaybackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CloverLikeService {

    private final CloverLikeRepository cloverLikeRepository;
    private final CloverRepository cloverRepository;
    private final UserRepository userRepository;

    @Transactional
    public void likeClover(Long cloverId, Long id) {
        Clover clover = cloverRepository.findById(cloverId)
                .orElseThrow(() -> new IllegalArgumentException("클로버가 존재하지 않습니다."));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        boolean alreadyLiked = cloverLikeRepository.existsByUserAndClover(user, clover);
        if (alreadyLiked) {
            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
        }

        CloverLike like = new CloverLike();
        like.setClover(clover);
        like.setUser(user);
        cloverLikeRepository.save(like);
    }

    @Transactional
    public void unlikeClover(Long cloverId, Long id) {
        Clover clover = cloverRepository.findById(cloverId)
                .orElseThrow(() -> new IllegalArgumentException("클로버가 존재하지 않습니다."));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        boolean exists = cloverLikeRepository.existsByUserAndClover(user, clover);
        if (!exists) {
            throw new IllegalStateException("좋아요를 누르지 않았습니다.");
        }

        cloverLikeRepository.deleteByUserAndClover(user, clover);
    }

    public long getLikeCount(Long cloverId) {
        return cloverLikeRepository.countByCloverId(cloverId);
    }

    public boolean hasUserLiked(Long cloverId, Long id) {
        Clover clover = cloverRepository.findById(cloverId)
                .orElseThrow(() -> new IllegalArgumentException("클로버가 존재하지 않습니다."));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        return cloverLikeRepository.existsByUserAndClover(user, clover);
    }
}
