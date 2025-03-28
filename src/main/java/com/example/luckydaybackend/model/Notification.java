package com.example.luckydaybackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long receiverId;
    private Long senderId;
    private String type;
    private Long targetId;

    private Boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
