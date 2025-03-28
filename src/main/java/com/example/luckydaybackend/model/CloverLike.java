package com.example.luckydaybackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "clover_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"clover_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CloverLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clover_id", nullable = false)
    private Clover clover;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Timestamp createdAt;
}
