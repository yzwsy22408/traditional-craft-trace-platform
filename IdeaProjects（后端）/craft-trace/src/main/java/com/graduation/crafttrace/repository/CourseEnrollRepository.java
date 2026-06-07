package com.graduation.crafttrace.repository;

import com.graduation.crafttrace.entity.CourseEnroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollRepository extends JpaRepository<CourseEnroll, Long> {

    // ✅ 根据课程和学生ID查询（适配你的实体类字段名）
    List<CourseEnroll> findByCourseIdAndStudentUserId(Long courseId, Long studentUserId);

    // ✅ 判断是否已经报名，用于批量导入时的去重
    boolean existsByCourseIdAndStudentUserId(Long courseId, Long studentUserId);

    // ✅ 按课程查看所有报名记录
    List<CourseEnroll> findByCourseIdOrderByIdAsc(Long courseId);
}