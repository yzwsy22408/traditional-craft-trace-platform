package com.graduation.crafttrace.repository;

import com.graduation.crafttrace.entity.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    Optional<CourseReview> findByCourseIdAndStudentUserId(Long courseId, Long studentUserId);

    List<CourseReview> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    // ✅ 核心方法：按学生 ID 和创建时间倒序查询
    List<CourseReview> findByStudentUserIdOrderByCreatedAtDesc(Long studentUserId);

    long countByCourseId(Long courseId);
}