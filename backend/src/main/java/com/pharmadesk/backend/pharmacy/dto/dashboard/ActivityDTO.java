package com.pharmadesk.backend.pharmacy.dto.dashboard;

import java.time.LocalDateTime;

public class ActivityDTO {
    private Long id;
    private String user;
    private String action;
    private LocalDateTime timestamp;

    public ActivityDTO() {}

    public ActivityDTO(Long id, String user, String action, LocalDateTime timestamp) {
        this.id = id;
        this.user = user;
        this.action = action;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
