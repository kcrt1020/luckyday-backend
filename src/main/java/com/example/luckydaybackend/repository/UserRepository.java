package com.example.luckydaybackend.repository;

import com.example.luckydaybackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
