package com.graduation.crafttrace.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "course_review",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"course_id", "student_user_id"})
        }
)
public class CourseReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="course_id", nullable = false)
    private Long courseId;

    private String courseName;

    @Column(name="student_user_id", nullable = false)
    private Long studentUserId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    /** 匠人回复内容 */
    @Column(columnDefinition = "TEXT")
    private String reply;

    /** ✅ 新增：记录回复的匠人ID */
    private Long replyArtisanId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (rating == null) rating = 5;
        if (content != null) content = content.trim();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        if (content != null) content = content.trim();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Long getStudentUserId() { return studentUserId; }
    public void setStudentUserId(Long studentUserId) { this.studentUserId = studentUserId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    /** ✅ 新增：replyArtisanId 的 Getter/Setter */
    public Long getReplyArtisanId() { return replyArtisanId; }
    public void setReplyArtisanId(Long replyArtisanId) { this.replyArtisanId = replyArtisanId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}