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

    public Clover createClover(Clover clover) {
        return cloverRepository.save(clover);
    }

    public List<Clover> getAllClovers() {
        return cloverRepository.findAllByOrderByCreatedAtDesc();
    }

    public Clover getCloverById(Long id) {
        return cloverRepository.findById(id).orElse(null);
    }

    public void deleteClover(Long id) {
        cloverRepository.deleteById(id);
    }
}