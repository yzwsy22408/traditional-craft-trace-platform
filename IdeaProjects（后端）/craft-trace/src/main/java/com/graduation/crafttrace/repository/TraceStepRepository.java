package com.graduation.crafttrace.repository;

import com.graduation.crafttrace.entity.TraceStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TraceStepRepository extends JpaRepository<TraceStep, Long> {

    /**
     * 查询某个工艺品的全部溯源步骤（按 stepNo 升序）
     * 给学生端/公共溯源页做时间线展示用
     */
    List<TraceStep> findByCraftItemIdOrderByStepNoAsc(Long craftItemId);

    /**
     * 查询某个工艺品的最后一步（stepNo 最大的那条）
     * 用于新增步骤时生成链式 stepHash，取 previousHash
     */
    Optional<TraceStep> findTopByCraftItemIdOrderByStepNoDesc(Long craftItemId);

    /**
     * ✅ 推荐：非 Optional 版本（写 Service 更顺手）
     * 如果没有记录会返回 null
     */
    TraceStep findFirstByCraftItemIdOrderByStepNoDesc(Long craftItemId);

    /**
     * （可选但推荐）按 craftItemId + stepNo 查询某一步
     * 后续如果你要做“修改某一步/防重复 stepNo”会用到
     */
    Optional<TraceStep> findByCraftItemIdAndStepNo(Long craftItemId, Integer stepNo);

    /**
     * ✅ 推荐：新增步骤前防止 stepNo 重复
     */
    boolean existsByCraftItemIdAndStepNo(Long craftItemId, Integer stepNo);
}
