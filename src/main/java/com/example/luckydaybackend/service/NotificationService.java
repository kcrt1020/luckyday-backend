package com.example.luckydaybackend.service;

import com.example.luckydaybackend.model.Notification;
import com.example.luckydaybackend.model.User;
import com.example.luckydaybackend.repository.NotificationRepository;
import com.example.luckydaybackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 알림 생성 (알림 받을 사람, 내용, URL)
     */
    public void sendNotification(Long receiverId, Long senderId, String type, Long targetId, String url) {
        // 받는 사람
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("알림 받을 유저를 찾을 수 없습니다."));

        // 보낸 사람
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("알림 보낸 유저를 찾을 수 없습니다."));

        // 알림 객체 생성
        Notification notification = Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .type(type)            // ex: "LIKE", "COMMENT", "FOLLOW"
                .targetId(targetId)    // ex: 클로버 ID or 유저 ID
                .url(url)              // ex: "/clovers/123"
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        // 저장
        notificationRepository.save(notification);
    }


    /**
     * 알림 목록 조회
     */
    public List<Notification> getNotifications(Long receiverId) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    public long countUnread(Long receiverId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(receiverId);
    }

    /**
     * 특정 알림 읽음 처리
     */
    public void markAsRead(Long notificationId, Long userId) {

        Notification noti = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림 없음"));

        System.out.println("알림 ID: " + notificationId);
        System.out.println("요청자 ID: " + userId);

        noti.setRead(true);
        notificationRepository.save(noti);
    }


}
