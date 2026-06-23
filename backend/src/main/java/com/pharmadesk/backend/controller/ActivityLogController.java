package com.pharmadesk.backend.controller;

import com.pharmadesk.backend.model.ActivityLog;
import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.repository.ActivityLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/activity-log")
public class ActivityLogController {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogController(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<ActivityLog>>> getLogsByUserId(
            @RequestParam Long userId,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<ActivityLog> logs;
        
        if (date != null && date.equals("today")) {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDateTime.now();
            logs = activityLogRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startOfDay, endOfDay, pageable);
        } else {
            logs = activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(logs, "Activity logs fetched"));
    }
}
