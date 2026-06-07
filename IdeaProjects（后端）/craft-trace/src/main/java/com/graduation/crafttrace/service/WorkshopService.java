package com.graduation.crafttrace.service;

import com.graduation.crafttrace.entity.Workshop;
import com.graduation.crafttrace.repository.WorkshopRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkshopService {

    private final WorkshopRepository workshopRepository;

    public WorkshopService(WorkshopRepository workshopRepository) {
        this.workshopRepository = workshopRepository;
    }

    public List<Workshop> listAll() {
        return workshopRepository.findAll();
    }

    public List<Workshop> listByOwner(Long ownerUserId) {
        return workshopRepository.findByOwnerUserId(ownerUserId);
    }

    // ✅✅ 未绑定匠人的工坊
    public List<Workshop> listUnbound() {
        return workshopRepository.findByOwnerUserIdIsNull();
    }

    public Workshop getById(Long id) {
        return workshopRepository.findById(id).orElse(null);
    }

    public Workshop create(Workshop w, Long ownerUserId) {
        w.setId(null);
        w.setOwnerUserId(ownerUserId);
        return workshopRepository.save(w);
    }

    public Workshop update(Long id, Workshop req) {
        Workshop w = workshopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("工坊不存在"));

        w.setName(req.getName());
        w.setPhone(req.getPhone());
        w.setAddress(req.getAddress());

        // ✅ 如果 Workshop 有 intro 字段就更新（没有也不会报错）
        try {
            Object intro = req.getClass().getMethod("getIntro").invoke(req);
            w.getClass().getMethod("setIntro", String.class).invoke(w, intro);
        } catch (Exception ignored) {}

        return workshopRepository.save(w);
    }

    public void delete(Long id) {
        workshopRepository.deleteById(id);
    }

    /**
     * ✅✅ 管理员绑定/更换匠人：实现“一一对应”
     * - 一个匠人只能绑定一个工坊
     * - 绑定新工坊前，先解绑该匠人之前的所有工坊（除了本次目标工坊）
     */
    public Workshop assignOwner(Long workshopId, Long newOwnerUserId) {
        Workshop target = workshopRepository.findById(workshopId)
                .orElseThrow(() -> new RuntimeException("工坊不存在"));

        // 1) 解绑该匠人之前绑定的工坊（保证一一对应）
        List<Workshop> oldList = workshopRepository.findByOwnerUserId(newOwnerUserId);
        for (Workshop w : oldList) {
            if (w.getId() != null && !w.getId().equals(workshopId)) {
                w.setOwnerUserId(null);
                workshopRepository.save(w);
            }
        }

        // 2) 绑定当前工坊
        target.setOwnerUserId(newOwnerUserId);
        return workshopRepository.save(target);
    }
}
