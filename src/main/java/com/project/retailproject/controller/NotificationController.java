package com.project.retailproject.controller;

import com.project.retailproject.common.ApiResponse;
import com.project.retailproject.dto.NotificationRequestDTO;
import com.project.retailproject.dto.NotificationResponseDTO;
import com.project.retailproject.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> insertNotification(
            @Valid @RequestBody NotificationRequestDTO dto) {
        NotificationResponseDTO data = notificationService.insertNotification(dto);
        return ResponseEntity.ok(ApiResponse.success("Notification created successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponseDTO>>> getAllNotifications() {
        List<NotificationResponseDTO> data = notificationService.getAllNotifications();
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> getNotification(
            @PathVariable Long id) {
        NotificationResponseDTO data = notificationService.getNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully", data));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> markAsRead(
            @PathVariable Long id) {
        NotificationResponseDTO data = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationResponseDTO>>> getByUser(
            @PathVariable Long userId) {
        List<NotificationResponseDTO> data = notificationService.getByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", data));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponseDTO>>> getUnread(
            @PathVariable Long userId) {
        List<NotificationResponseDTO> data = notificationService.getUnread(userId);
        return ResponseEntity.ok(ApiResponse.success("Unread notifications retrieved", data));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<NotificationResponseDTO>>> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "notificationId") String sorting,
            @RequestParam(defaultValue = "true") boolean asc) {
        Sort sort = asc ? Sort.by(sorting).ascending() : Sort.by(sorting).descending();
        Page<NotificationResponseDTO> data = notificationService.getAllNotificationsPaginated(
                PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", data));
    }
}