package com.example.luckydaybackend.repository;

import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUser(User user);

    List<UserProfile> findByNicknameContainingIgnoreCase(String nickname);

    Optional<Object> findByUser_Email(String userEmail);
}