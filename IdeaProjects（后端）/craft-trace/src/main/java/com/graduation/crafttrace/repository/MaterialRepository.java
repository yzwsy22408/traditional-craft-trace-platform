package com.graduation.crafttrace.repository;

import com.graduation.crafttrace.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long>, JpaSpecificationExecutor<Material> {

    // ✅ 关键：根据名称查找材料，用于自动扣减库存
    Optional<Material> findByName(String name);
}