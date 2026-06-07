package com.graduation.crafttrace.repository;

import com.graduation.crafttrace.entity.CraftItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CraftItemRepository extends JpaRepository<CraftItem, Long> {
    Optional<CraftItem> findByCode(String code);

    @Query(value = "SELECT " +
            "  CASE " +
            "    WHEN category IS NULL OR TRIM(category) = '' THEN '未分类工艺' " +
            "    ELSE TRIM(category) " +
            "  END AS name, " +
            "  COUNT(*) AS value " +
            "FROM craft_item " +
            "GROUP BY CASE WHEN category IS NULL OR TRIM(category) = '' THEN '未分类工艺' ELSE TRIM(category) END " +
            "ORDER BY value DESC, name ASC",
            nativeQuery = true)
    List<Map<String, Object>> countGroupByCategory();
}
