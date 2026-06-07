package com.graduation.crafttrace.repository;

import com.graduation.crafttrace.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    // ✅ 支持分页 + 关键字搜索（匹配 username / name / role）
    Page<User> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrRoleContainingIgnoreCase(
            String u, String n, String r, Pageable pageable
    );

    // ✅ 匠人/学员专用分页
    Page<User> findByRole(String role, Pageable pageable);

    // ✅ role 固定 + username/name 搜索
    Page<User> findByRoleAndUsernameContainingIgnoreCaseOrRoleAndNameContainingIgnoreCase(
            String role1, String usernameKw,
            String role2, String nameKw,
            Pageable pageable
    );
}
