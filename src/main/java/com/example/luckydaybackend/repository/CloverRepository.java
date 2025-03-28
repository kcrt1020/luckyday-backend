package com.example.luckydaybackend.repository;

import com.example.luckydaybackend.model.Clover;
import com.example.luckydaybackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CloverRepository extends JpaRepository<Clover, Long> {

    // ✅ 최신순으로 모든 클로버 조회
    List<Clover> findAllByOrderByCreatedAtDesc();

    // ✅ 이메일 기반으로 클로버 조회 (최신순)
    List<Clover> findByUserOrderByCreatedAtDesc(User user);

    List<Clover> findByParentCloverId(Long parentId);

    // 키워드가 content에 포함된 게시글을 최신순으로 조회
    List<Clover> findByContentContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);
}
