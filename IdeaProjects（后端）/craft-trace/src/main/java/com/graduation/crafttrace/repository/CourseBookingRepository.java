package com.graduation.crafttrace.repository;

import com.graduation.crafttrace.entity.CourseBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseBookingRepository extends JpaRepository<CourseBooking, Long> {

    // ✅ 学生：我的报名
    List<CourseBooking> findByStudentUserIdOrderByBookingTimeDesc(Long studentUserId);

    // ✅ 课程+学生：查报名记录（用于防重复/取消/恢复）
    Optional<CourseBooking> findByCourseIdAndStudentUserId(Long courseId, Long studentUserId);

    // ✅ 统计某课程的已报名人数（容量判断）——（这是“记录数”，不去重）
    long countByCourseIdAndStatus(Long courseId, String status);

    // ✅ 匠人/管理员：按课程查看报名列表
    List<CourseBooking> findByCourseIdOrderByBookingTimeDesc(Long courseId);

    // ✅✅ 新增：报名人数（学生去重，不看状态）
    long countDistinctStudentUserIdByCourseId(Long courseId);

    // ✅✅ 新增：报名人数（学生去重 + 按状态）
    long countDistinctStudentUserIdByCourseIdAndStatus(Long courseId, String status);
}
