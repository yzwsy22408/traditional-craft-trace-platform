package com.graduation.crafttrace.repository;

import com.graduation.crafttrace.entity.StudentWork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentWorkRepository extends JpaRepository<StudentWork, Long> {

    List<StudentWork> findByStudentUserIdOrderByUpdatedAtDesc(Long studentUserId);

    List<StudentWork> findByCourseIdOrderByUpdatedAtDesc(Long courseId);

    Optional<StudentWork> findByCourseIdAndStudentUserId(Long courseId, Long studentUserId);
}
