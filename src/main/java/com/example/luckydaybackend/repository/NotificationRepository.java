package com.example.luckydaybackend.repository;

import com.example.luckydaybackend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    Long countByReceiverIdAndIsReadFalse(Long receiverId);
}
