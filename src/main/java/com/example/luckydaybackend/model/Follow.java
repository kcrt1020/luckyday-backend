package com.example.luckydaybackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 팔로우 관계 ID

    @ManyToOne
    @JoinColumn(name = "from_user_id")
    private User fromUser;  // 팔로우한 사용자

    @ManyToOne
    @JoinColumn(name = "to_user_id")
    private User toUser;    // 팔로우 대상

    @CreationTimestamp
    private LocalDateTime createdAt;  // 팔로우한 시간

    // fromUser, toUser를 받아서 팔로우 관계 생성
    public Follow(User fromUser, User toUser) {
        this.fromUser = fromUser;
        this.toUser = toUser;
    }
}