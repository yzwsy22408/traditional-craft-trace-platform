package com.graduation.crafttrace.dto;

import java.time.LocalDateTime;

public class CourseAttendanceVO {
    private Long id;
    private Long courseId;
    private Long userId;
    private String username;
    private String status;
    private LocalDateTime signTime;

    public CourseAttendanceVO() {}

    public CourseAttendanceVO(Long id, Long courseId, Long userId, String username, String status, LocalDateTime signTime) {
        this.id = id;
        this.courseId = courseId;
        this.userId = userId;
        this.username = username;
        this.status = status;
        this.signTime = signTime;
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getStatus() { return status; }
    public LocalDateTime getSignTime() { return signTime; }

    public void setId(Long id) { this.id = id; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setStatus(String status) { this.status = status; }
    public void setSignTime(LocalDateTime signTime) { this.signTime = signTime; }
}
