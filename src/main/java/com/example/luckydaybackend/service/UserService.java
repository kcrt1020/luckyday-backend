package com.example.luckydaybackend.service;

import com.example.luckydaybackend.auth.model.User;
import com.example.luckydaybackend.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null); // ✅ 유저가 없으면 null 반환
    }
}