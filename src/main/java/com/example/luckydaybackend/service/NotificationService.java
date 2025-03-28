package com.example.luckydaybackend.service;

import com.example.luckydaybackend.model.Notification;
import com.example.luckydaybackend.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // 알림 생성
    public void createNotification(Long receiverId, Long senderId, String type, Long targetId) {
        if (receiverId.equals(senderId)) return; // 자기 자신한테 알림 안 줌

        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .type(type)
                .targetId(targetId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    // 유저의 알림 목록
    public List<Notification> getNotifications(Long receiverId) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
    }

    // 특정 알림 읽음 처리
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다."));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    // 안 읽은 알림 개수
    public Long countUnread(Long receiverId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(receiverId);
    }
}
