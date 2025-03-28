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

    // Id로 유저 조회
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("대상 유저 없음"));
    }

    // Id로 유저 조회
    public User getUserByUserId(String username) {
        return userRepository.findByUsername(username)
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
        List<Follow> follows = followRepository.findByFromUser(fromUser);

        List<Long> toUserIds = follows.stream()
                .map(f -> f.getToUser().getId())
                .toList();
        System.out.println("📌 [팔로잉 목록 조회] 팔로우한 유저 ID 목록: " + toUserIds);

        // 3. 대상 유저 + 프로필 정보 가져오기
        List<User> users = userRepository.findAllWithProfileByIdIn(toUserIds);
        System.out.println("📌 [팔로잉 목록 조회] 최종 반환 유저 수 (프로필 포함): " + users.size());

        return users;
    }


    // 팔로워 목록 조회
    public List<User> getFollowersList(User toUser) {
        List<Follow> follows = followRepository.findByToUser(toUser);
        List<User> fromUsers = follows.stream()
                .map(Follow::getFromUser)
                .toList();

        return userRepository.findAllWithProfileByUsers(fromUsers);
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