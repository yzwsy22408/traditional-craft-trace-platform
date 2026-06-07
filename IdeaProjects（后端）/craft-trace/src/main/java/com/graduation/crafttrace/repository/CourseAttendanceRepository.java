package com.graduation.crafttrace.repository;

import com.graduation.crafttrace.entity.CourseAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseAttendanceRepository extends JpaRepository<CourseAttendance, Long> {

    Optional<CourseAttendance> findByCourseIdAndUserId(Long courseId, Long userId);

    List<CourseAttendance> findByCourseId(Long courseId);

    List<CourseAttendance> findByUserId(Long userId);

    // ✅✅ 统计：签到人数（学生去重）
    long countDistinctUserIdByCourseIdAndStatus(Long courseId, String status);
}
