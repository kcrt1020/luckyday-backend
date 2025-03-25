package com.example.luckydaybackend.repository;

import com.example.luckydaybackend.model.Follow;
import com.example.luckydaybackend.model.FollowId;
import com.example.luckydaybackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    // fromUser가 팔로우한 대상들 조회
    List<Follow> findByFromUser(User fromUser);

    // toUser를 팔로우한 사람들 조회
    List<Follow> findByToUser(User toUser);

    // fromUser와 toUser의 팔로우 관계가 존재하는지 확인
    boolean existsByFromUserAndToUser(User fromUser, User toUser);

    // 팔로우 관계를 삭제하려면 fromUser와 toUser로 찾기
    Optional<Follow> findByFromUserAndToUser(User fromUser, User toUser);

    // 팔로우 관계 저장
    Follow save(Follow follow);

    // 팔로우 관계 삭제
    void delete(Follow follow);

    // fromUser가 팔로우한 수
    long countByFromUser(User fromUser);

    // toUser의 팔로워 수
    long countByToUser(User toUser);
}
