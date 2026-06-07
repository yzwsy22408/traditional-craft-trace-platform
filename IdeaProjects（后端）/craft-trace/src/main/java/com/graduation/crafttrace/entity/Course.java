package com.graduation.crafttrace.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long workshopId;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String intro;

    private String coverUrl;

    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer capacity = 0;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Column(length = 50)
    private String category = "其他";

    @Column(nullable = false)
    private String status = "DRAFT";

    private Long createdBy;

    private Long teacherId;

    @Column(length = 100)
    private String teacherName;

    @Column(length = 150)
    private String teacherSchoolName;

    @Column(length = 100)
    private String teacherSubjectName;

    @Column(columnDefinition = "TEXT")
    private String leadRoute;

    @Column(columnDefinition = "TEXT")
    private String teacherNote;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null || status.trim().isEmpty()) status = "DRAFT";
        trimFields();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        trimFields();
    }

    private void trimFields() {
        title = normalize(title);
        intro = normalize(intro);
        coverUrl = normalize(coverUrl);
        category = normalize(category);
        status = normalize(status);
        teacherName = normalize(teacherName);
        teacherSchoolName = normalize(teacherSchoolName);
        teacherSubjectName = normalize(teacherSubjectName);
        leadRoute = normalize(leadRoute);
        teacherNote = normalize(teacherNote);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkshopId() { return workshopId; }
    public void setWorkshopId(Long workshopId) { this.workshopId = workshopId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getTeacherSchoolName() { return teacherSchoolName; }
    public void setTeacherSchoolName(String teacherSchoolName) { this.teacherSchoolName = teacherSchoolName; }
    public String getTeacherSubjectName() { return teacherSubjectName; }
    public void setTeacherSubjectName(String teacherSubjectName) { this.teacherSubjectName = teacherSubjectName; }
    public String getLeadRoute() { return leadRoute; }
    public void setLeadRoute(String leadRoute) { this.leadRoute = leadRoute; }
    public String getTeacherNote() { return teacherNote; }
    public void setTeacherNote(String teacherNote) { this.teacherNote = teacherNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
