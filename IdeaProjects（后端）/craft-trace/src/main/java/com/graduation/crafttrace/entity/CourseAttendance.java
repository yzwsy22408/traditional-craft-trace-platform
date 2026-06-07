package com.graduation.crafttrace.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "course_attendance",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"course_id", "user_id"})
        }
)
public class CourseAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** SIGNED */
    @Column(nullable = false)
    private String status;

    @Column(name = "sign_time")
    private LocalDateTime signTime;

    // ===== getter / setter =====

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSignTime() {
        return signTime;
    }

    public void setSignTime(LocalDateTime signTime) {
        this.signTime = signTime;
    }
}
