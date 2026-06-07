package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.entity.CourseEnroll;
import com.graduation.crafttrace.service.CourseEnrollService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enroll")
public class CourseEnrollController {

    private final CourseEnrollService service;

    public CourseEnrollController(CourseEnrollService service) {
        this.service = service;
    }

    // ✅ 获取某课程的所有报名名单
    @GetMapping("/course/{courseId}")
    public List<CourseEnroll> listByCourse(@PathVariable Long courseId) {
        return service.listByCourse(courseId);
    }

    // ✅ 团体管理：批量报名接口
    // 请求体样例：{"courseId": 1, "studentIds": [10, 11, 12]}
    @PostMapping("/batch")
    public Map<String, Object> batchEnroll(@RequestBody Map<String, Object> payload) {
        Long courseId = Long.valueOf(payload.get("courseId").toString());
        List<Integer> ids = (List<Integer>) payload.get("studentIds");

        // 类型转换
        List<Long> studentIds = ids.stream()
                .map(Integer::longValue)
                .collect(java.util.stream.Collectors.toList());

        service.batchEnroll(courseId, studentIds);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "批量报名/导入成功");
        return resp;
    }

    // ✅ 签到
    @PostMapping("/{id}/sign")
    public CourseEnroll sign(@PathVariable Long id) {
        return service.markSigned(id);
    }
}