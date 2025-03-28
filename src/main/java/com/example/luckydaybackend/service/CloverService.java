package com.example.luckydaybackend.service;

import com.example.luckydaybackend.model.Clover;
import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.repository.CloverRepository;
import com.example.luckydaybackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CloverService {

    private final CloverRepository cloverRepository;
    private final UserRepository userRepository;

    /**
     * 클로버 생성
     */
    public Clover createClover(Clover clover) {
        return cloverRepository.save(clover);
    }

    /**
     * 모든 클로버 조회 (최신순)
     */
    public List<Clover> getAllClovers() {
        return cloverRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 특정 클로버 조회 (ID 기반)
     */
    public Clover getCloverById(Long id) {
        return cloverRepository.findById(id).orElse(null);
    }

    /**
     * 특정 클로버 삭제
     */
    public void deleteClover(Long id) {
        cloverRepository.deleteById(id);
    }

    /**
     * 유저 ID 기반 클로버 조회 (최신순)
     */
    public List<Clover> getCloversByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
        return cloverRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * 클로버 ID로 작성자 ID 조회
     */
    public Long getAuthorIdByCloverId(Long cloverId) {
        Clover clover = cloverRepository.findById(cloverId)
                .orElseThrow(() -> new IllegalArgumentException("클로버가 존재하지 않습니다."));
        return clover.getUser().getId();
    }

    /**
     * 답글 조회
     */
    public List<Clover> getRepliesByParentId(Long parentId) {
        return cloverRepository.findByParentCloverId(parentId);
    }

    /**
     * 키워드로 클로버 검색
     */
    public List<Clover> searchCloversByKeyword(String keyword) {
        return cloverRepository.findByContentContainingIgnoreCaseOrderByCreatedAtDesc(keyword);
    }

    /**
     * ID로 조회 (nullable 허용)
     */
    public Clover findById(Long id) {
        return cloverRepository.findById(id).orElse(null);
    }
}
