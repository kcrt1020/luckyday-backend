package com.example.luckydaybackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Clover {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_clover_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "parentClover"})
    private Clover parentClover;  // 대댓글의 경우 부모 클로버

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // FK: users.id
    @JsonIgnore
    private User user;
}
