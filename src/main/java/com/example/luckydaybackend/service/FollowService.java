package com.example.luckydaybackend.service;

import com.example.luckydaybackend.model.Follow;
import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.repository.FollowRepository;
import com.example.luckydaybackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class FollowService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    // userId로 유저 조회
    public User getUserByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("대상 유저 없음"));
    }

    // 팔로우 상태 확인
    public boolean isFollowing(String fromEmail, User targetUser) {
        User fromUser = userRepository.findByEmail(fromEmail)
                .orElseThrow(() -> new RuntimeException("요청 유저 없음"));
        return followRepository.existsByFromUserAndToUser(fromUser, targetUser);
    }

    // 팔로우 하기
    public void follow(String fromEmail, User targetUser) {
        User fromUser = userRepository.findByEmail(fromEmail)
                .orElseThrow(() -> new RuntimeException("요청 유저 없음"));

        // 팔로우 추가
        followRepository.save(new Follow(fromUser, targetUser));
    }

    // 언팔로우 하기
    public void unfollow(String fromEmail, User targetUser) {
        User fromUser = userRepository.findByEmail(fromEmail)
                .orElseThrow(() -> new RuntimeException("요청 유저 없음"));

        // 팔로우 삭제
        Follow follow = followRepository.findByFromUserAndToUser(fromUser, targetUser)
                .orElseThrow(() -> new RuntimeException("팔로우 관계가 존재하지 않음"));

        followRepository.delete(follow);
    }

    // 팔로잉 목록 조회
    public List<User> getFollowingList(User fromUser) {
        List<Follow> follows = followRepository.findByFromUser(fromUser);  // fromUser가 팔로우한 사람들
        return follows.stream()
                .map(Follow::getToUser)  // 팔로우한 대상인 toUser 반환
                .toList();
    }

    // 팔로워 목록 조회
    public List<User> getFollowersList(User toUser) {
        List<Follow> follows = followRepository.findByToUser(toUser);  // toUser가 팔로워한 사람들
        return follows.stream()
                .map(Follow::getFromUser)  // 팔로워인 fromUser 반환
                .toList();
    }

    // 팔로잉 수 조회
    public long getFollowingCount(User fromUser) {
        return followRepository.countByFromUser(fromUser);
    }

    // 팔로워 수 조회
    public long getFollowersCount(User toUser) {
        return followRepository.countByToUser(toUser);
    }
}