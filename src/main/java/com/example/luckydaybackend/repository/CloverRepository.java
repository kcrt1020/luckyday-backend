package com.example.luckydaybackend.repository;

import com.example.luckydaybackend.model.Clover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CloverRepository extends JpaRepository<Clover, Long> {

    // ✅ 최신순으로 모든 클로버 조회
    List<Clover> findAllByOrderByCreatedAtDesc();

    // ✅ 이메일 기반으로 클로버 조회 (최신순)
    List<Clover> findByEmailOrderByCreatedAtDesc(String email);

}
