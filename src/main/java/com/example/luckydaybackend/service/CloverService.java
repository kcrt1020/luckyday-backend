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
//        System.out.println("🔍 Clover 조회 - 이메일: " + email);
        List<Clover> clovers = cloverRepository.findByEmailOrderByCreatedAtDesc(email);

        if (clovers.isEmpty()) {
            System.out.println("⚠️ 클로버 없음! DB에 데이터 확인 필요.");
        } else {
//            System.out.println("✅ 가져온 클로버 데이터: " + clovers);
        }

        return clovers;
    }

    public Clover findById(Long id) {
        return cloverRepository.findById(id)
                .orElse(null);
    }

    public List<Clover> getRepliesByParentId(Long parentId) {
        return cloverRepository.findByParentCloverId(parentId);
    }

    public List<Clover> searchCloversByKeyword(String keyword) {
        return cloverRepository.findByContentContainingIgnoreCaseOrderByCreatedAtDesc(keyword);
    }


}
