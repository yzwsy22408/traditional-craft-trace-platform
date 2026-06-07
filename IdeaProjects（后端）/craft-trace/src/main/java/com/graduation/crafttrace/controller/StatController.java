package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.repository.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/stat")
public class StatController {

    private final WorkshopRepository workshopRepo;
    private final CourseRepository courseRepo;
    private final CourseAttendanceRepository attendanceRepo;
    private final CraftItemRepository craftRepo;

    public StatController(WorkshopRepository workshopRepo,
                          CourseRepository courseRepo,
                          CourseAttendanceRepository attendanceRepo,
                          CraftItemRepository craftRepo) {
        this.workshopRepo = workshopRepo;
        this.courseRepo = courseRepo;
        this.attendanceRepo = attendanceRepo;
        this.craftRepo = craftRepo;
    }

    /** * 首页大屏数据汇总接口 - 彻底动态化
     */
    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        Map<String, Object> data = new HashMap<>();

        // 1. 基础计数
        data.put("workshopCount", workshopRepo.count());
        data.put("courseCount", courseRepo.count());
        data.put("attendanceCount", attendanceRepo.count());
        data.put("craftCount", craftRepo.count());

        // 2. 按工艺档案而不是课程分类做聚合，保证首页图表和“工艺档案”语义一致
        List<Map<String, Object>> stats = craftRepo.countGroupByCategory();

        // 提取分类名数组
        data.put("categories", stats.stream()
                .map(m -> m.get("name"))
                .collect(Collectors.toList()));

        // 提取对应的数值数组
        data.put("categoryData", stats.stream()
                .map(m -> m.get("value"))
                .collect(Collectors.toList()));

        // 3. 注入排行榜
        data.put("leaderboard", courseRepo.findTop5ByStatusOrderByCreatedAtDesc("PUBLISHED"));

        return data;
    }
}
