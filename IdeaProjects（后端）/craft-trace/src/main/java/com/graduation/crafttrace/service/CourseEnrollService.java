package com.graduation.crafttrace.service;

import com.graduation.crafttrace.User;
import com.graduation.crafttrace.entity.CourseEnroll;
import com.graduation.crafttrace.repository.CourseEnrollRepository;
import com.graduation.crafttrace.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseEnrollService {

    private final CourseEnrollRepository repo;
    private final UserRepository userRepository;

    public CourseEnrollService(CourseEnrollRepository repo, UserRepository userRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
    }

    /**
     * ✅ 核心功能：学员团体批量报名
     * 自动处理：去重、姓名填充、状态更新
     */
    @Transactional
    public void batchEnroll(Long courseId, List<Long> studentIds) {
        for (Long studentId : studentIds) {
            // 1. 如果该学生已经报名过该课程，则跳过，避免数据重复
            if (repo.existsByCourseIdAndStudentUserId(courseId, studentId)) {
                continue;
            }

            // 2. 获取学生详细信息，获取姓名
            User student = userRepository.findById(studentId).orElse(null);
            String realName = (student != null) ? (student.getName() != null ? student.getName() : student.getUsername()) : "未知学员";

            // 3. 创建报名记录
            CourseEnroll e = new CourseEnroll();
            e.setCourseId(courseId);
            e.setStudentUserId(studentId);
            e.setStudentName(realName);
            e.setStatus("PAID"); // 团体报名通常默认已支付
            e.setPayTime(LocalDateTime.now());

            repo.save(e);
        }
    }

    public List<CourseEnroll> listByCourse(Long courseId) {
        return repo.findByCourseIdOrderByIdAsc(courseId);
    }

    @Transactional
    public CourseEnroll markSigned(Long enrollId) {
        CourseEnroll e = repo.findById(enrollId).orElseThrow(() -> new RuntimeException("报名记录不存在"));
        e.setStatus("SIGNED"); //
        e.setSignTime(LocalDateTime.now());
        return repo.save(e);
    }
}