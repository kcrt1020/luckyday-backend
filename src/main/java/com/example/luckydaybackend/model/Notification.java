package com.example.luckydaybackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;        // "LIKE", "COMMENT", "FOLLOW"
    private Long targetId;      // 해당 클로버 ID or 유저 ID
    @ManyToOne(fetch = FetchType.LAZY)
    private User sender;
    @ManyToOne(fetch = FetchType.LAZY)
    private User receiver;
    private String url;
    private boolean isRead;
    private LocalDateTime createdAt;

}
