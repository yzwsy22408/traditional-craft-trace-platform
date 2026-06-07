package com.graduation.crafttrace.repository;

import com.graduation.crafttrace.entity.Workshop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkshopRepository extends JpaRepository<Workshop, Long> {

    List<Workshop> findByOwnerUserId(Long ownerUserId);

    // ✅✅ 未绑定匠人的工坊
    List<Workshop> findByOwnerUserIdIsNull();
}
