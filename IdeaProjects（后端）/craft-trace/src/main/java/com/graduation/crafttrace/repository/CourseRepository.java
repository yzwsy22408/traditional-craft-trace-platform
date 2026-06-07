package com.graduation.crafttrace.repository;

import com.graduation.crafttrace.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // 学生端公开课程列表：只看已发布
    List<Course> findByStatusOrderByStartTimeAsc(String status);

    // ✅ 匠人端：只看自己创建的课程
    List<Course> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    /**
     * ✅ 核心修复：实时分组统计
     * 逻辑：如果 category 为空或 null，则归类为“基础研学”，否则按真实分类名统计
     */
    @Query(value = "SELECT " +
            "  CASE " +
            "    WHEN category IS NULL OR category = '' THEN '基础研学' " +
            "    ELSE category " +
            "  END as name, " +
            "  COUNT(*) as value " +
            "FROM course " +
            "GROUP BY CASE WHEN category IS NULL OR category = '' THEN '基础研学' ELSE category END",
            nativeQuery = true)
    List<Map<String, Object>> countGroupByCategory();

    // ✅ 获取最新发布的 5 个热门课程
    List<Course> findTop5ByStatusOrderByCreatedAtDesc(String status);
}