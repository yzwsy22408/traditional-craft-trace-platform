package com.graduation.crafttrace.repository;

import com.graduation.crafttrace.entity.CourseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CourseOrderRepository extends JpaRepository<CourseOrder, Long> {

    List<CourseOrder> findByStudentUserIdOrderByCreatedAtDesc(Long studentUserId);

    // ✅ 防止重复创建 UNPAID：同一课程同一人，取最新的未支付订单
    Optional<CourseOrder> findTopByCourseIdAndStudentUserIdAndStatusOrderByCreatedAtDesc(
            Long courseId, Long studentUserId, String status
    );

    // ✅ 统计某课程已支付订单数（订单数，不去重）
    long countByCourseIdAndStatus(Long courseId, String status);

    // ✅✅ 已支付人数（学生去重）
    long countDistinctStudentUserIdByCourseIdAndStatus(Long courseId, String status);

    // ✅✅ 新增：拿到某课程某状态的订单列表（用于“报名+支付”交集统计）
    List<CourseOrder> findByCourseIdAndStatus(Long courseId, String status);
}
