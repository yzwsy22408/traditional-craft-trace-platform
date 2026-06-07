package com.graduation.crafttrace.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 150)
    private String schoolName;

    @Column(length = 100)
    private String subjectName;

    @Column(length = 50)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String leadRoute;

    @Column(columnDefinition = "TEXT")
    private String workshopFocus;

    @Column(columnDefinition = "TEXT")
    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        trimFields();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        trimFields();
    }

    private void trimFields() {
        name = normalize(name);
        schoolName = normalize(schoolName);
        subjectName = normalize(subjectName);
        phone = normalize(phone);
        leadRoute = normalize(leadRoute);
        workshopFocus = normalize(workshopFocus);
        note = normalize(note);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLeadRoute() { return leadRoute; }
    public void setLeadRoute(String leadRoute) { this.leadRoute = leadRoute; }
    public String getWorkshopFocus() { return workshopFocus; }
    public void setWorkshopFocus(String workshopFocus) { this.workshopFocus = workshopFocus; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
