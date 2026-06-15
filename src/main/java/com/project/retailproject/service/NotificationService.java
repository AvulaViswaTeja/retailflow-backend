package com.project.retailproject.service;

import com.project.retailproject.db.NotificationRepository;
import com.project.retailproject.db.UserRepository;
import com.project.retailproject.dto.NotificationRequestDTO;
import com.project.retailproject.dto.NotificationResponseDTO;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.Notification;
import com.project.retailproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public NotificationResponseDTO insertNotification(NotificationRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + dto.getUserId()));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(dto.getMessage());
        notification.setCategory(dto.getCategory());
        notification.setStatus("UNREAD");
        notification.setCreatedDate(LocalDateTime.now());

        return mapToDTO(notificationRepository.save(notification));
    }




    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification not found with ID: " + id);
        }
        notificationRepository.deleteById(id);
    }

    public NotificationResponseDTO markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with ID: " + id));
        notification.setStatus("READ");
        return mapToDTO(notificationRepository.save(notification));
    }

    public NotificationResponseDTO getNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with ID: " + id));
        return mapToDTO(notification);
    }

    public List<NotificationResponseDTO> getAllNotifications() {
        return notificationRepository.findAll()
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationResponseDTO> getByUser(Long userId) {
        return notificationRepository.findByUserUserId(userId)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationResponseDTO> getUnread(Long userId) {
        return notificationRepository.findByUserUserId(userId)
                .stream()
                .filter(n -> n.getStatus().equals("UNREAD"))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<NotificationResponseDTO> getAllNotificationsPaginated(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(this::mapToDTO);
    }


    private NotificationResponseDTO mapToDTO(Notification n) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setNotificationId(n.getNotificationId());
        dto.setMessage(n.getMessage());
        dto.setCategory(n.getCategory());
        dto.setStatus(n.getStatus());
        dto.setCreatedDate(n.getCreatedDate());
        if (n.getUser() != null) {
            dto.setUserId(n.getUser().getUserId());
            dto.setUserName(n.getUser().getUserName());
        }
        return dto;
    }
}