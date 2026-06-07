package com.graduation.crafttrace.repository;

import com.graduation.crafttrace.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findFirstByName(String name);
}
