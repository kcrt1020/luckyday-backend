package com.example.luckydaybackend.controller;

import com.example.luckydaybackend.auth.UserPrincipal;
import com.example.luckydaybackend.model.Notification;
import com.example.luckydaybackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 목록 조회
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@AuthenticationPrincipal UserPrincipal user) {
        List<Notification> notifications = notificationService.getNotifications(user.getId());
        return ResponseEntity.ok(notifications);
    }

    // 특정 알림 읽음 처리
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // 안 읽은 알림 개수
    @GetMapping("/unread-count")
    public ResponseEntity<Long> countUnread(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(notificationService.countUnread(user.getId()));
    }
}
