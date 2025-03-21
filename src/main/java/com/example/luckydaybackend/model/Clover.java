package com.example.luckydaybackend.model;


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

    private String email;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_clover_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "parentClover"})
    private Clover parentClover;  // 댓글일 경우 부모 클로버 참조
}
