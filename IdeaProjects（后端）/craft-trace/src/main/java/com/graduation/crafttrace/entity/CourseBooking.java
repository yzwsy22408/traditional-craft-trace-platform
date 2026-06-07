package com.graduation.crafttrace.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_booking",
        uniqueConstraints = @UniqueConstraint(name = "uk_booking", columnNames = {"course_id", "student_user_id"}))
public class CourseBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="course_id", nullable = false)
    private Long courseId;

    @Column(name="student_user_id", nullable = false)
    private Long studentUserId;

    private LocalDateTime bookingTime;

    @Column(nullable = false)
    private String status; // BOOKED / CANCELED / ATTENDED

    @PrePersist
    public void prePersist() {
        if (bookingTime == null) bookingTime = LocalDateTime.now();
        if (status == null || status.trim().isEmpty()) status = "BOOKED";
    }

    public Long getId() { return id; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getStudentUserId() { return studentUserId; }
    public void setStudentUserId(Long studentUserId) { this.studentUserId = studentUserId; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
