package com.example.luckydaybackend.service;

import com.example.luckydaybackend.model.Clover;
import com.example.luckydaybackend.repository.CloverRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CloverService {
    private final CloverRepository cloverRepository;

    public CloverService(CloverRepository cloverRepository) {
        this.cloverRepository = cloverRepository;
    }

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
     * 이메일 기반 클로버 조회
     */
    public List<Clover> getCloversByEmail(String email) {
        return cloverRepository.findByEmailOrderByCreatedAtDesc(email); // ✅ 이메일 기반으로 변경
    }
}
