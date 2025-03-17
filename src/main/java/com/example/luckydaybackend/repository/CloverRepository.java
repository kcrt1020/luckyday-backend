package com.example.luckydaybackend.repository;

import com.example.luckydaybackend.model.Clover;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CloverRepository extends JpaRepository<Clover, Long> {
    List<Clover> findAllByOrderByCreatedAtDesc();
}