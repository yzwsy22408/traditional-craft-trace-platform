package com.graduation.crafttrace.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "course_enroll")
public class CourseEnroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "student_user_id", nullable = false)
    private Long studentUserId;

    @Column(name = "student_name")
    private String studentName;

    // ENROLLED / PAID / SIGNED / CANCELED
    @Column(name = "status", nullable = false)
    private String status = "ENROLLED";

    @Column(name = "pay_time")
    private LocalDateTime payTime;

    @Column(name = "sign_time")
    private LocalDateTime signTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
        if (this.status == null || this.status.trim().isEmpty()) this.status = "ENROLLED";
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
