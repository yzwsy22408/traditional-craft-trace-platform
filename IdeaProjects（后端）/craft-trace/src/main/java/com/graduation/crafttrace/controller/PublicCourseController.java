package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.entity.Course;
import com.graduation.crafttrace.repository.CourseAttendanceRepository;
import com.graduation.crafttrace.repository.CourseBookingRepository;
import com.graduation.crafttrace.repository.CourseRepository;
import com.graduation.crafttrace.repository.CourseReviewRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/public")
public class PublicCourseController {

    private final CourseRepository courseRepository;
    private final CourseBookingRepository courseBookingRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final CourseAttendanceRepository courseAttendanceRepository;

    public PublicCourseController(
            CourseRepository courseRepository,
            CourseBookingRepository courseBookingRepository,
            CourseReviewRepository courseReviewRepository,
            CourseAttendanceRepository courseAttendanceRepository
    ) {
        this.courseRepository = courseRepository;
        this.courseBookingRepository = courseBookingRepository;
        this.courseReviewRepository = courseReviewRepository;
        this.courseAttendanceRepository = courseAttendanceRepository;
    }

    @GetMapping("/courses")
    public List<Course> listPublished() {
        return courseRepository.findByStatusOrderByStartTimeAsc("PUBLISHED");
    }

    @GetMapping("/courses/{id}")
    public Course detail(@PathVariable Long id) {
        if (id == null) {
            throw new ResponseStatusException(BAD_REQUEST, "课程编号不能为空");
        }

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "未找到对应课程，ID=" + id));

        if (!"PUBLISHED".equals(course.getStatus())) {
            throw new ResponseStatusException(BAD_REQUEST, "当前课程尚未发布，暂不支持公开查看");
        }
        return course;
    }

    @GetMapping("/course-heat")
    public List<Map<String, Object>> courseHeat() {
        return courseRepository.findByStatusOrderByStartTimeAsc("PUBLISHED").stream()
                .map(this::buildHeatRow)
                .sorted((left, right) -> Long.compare(
                        ((Number) right.get("heatScore")).longValue(),
                        ((Number) left.get("heatScore")).longValue()
                ))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildHeatRow(Course course) {
        long bookingCount = courseBookingRepository.countDistinctStudentUserIdByCourseId(course.getId());
        long signedCount = courseAttendanceRepository.countDistinctUserIdByCourseIdAndStatus(course.getId(), "SIGNED");
        long reviewCount = courseReviewRepository.countByCourseId(course.getId());
        long heatScore = bookingCount * 4 + signedCount * 6 + reviewCount * 3;

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", course.getId());
        row.put("title", course.getTitle());
        row.put("category", course.getCategory());
        row.put("coverUrl", course.getCoverUrl());
        row.put("teacherName", course.getTeacherName());
        row.put("startTime", course.getStartTime());
        row.put("bookingCount", bookingCount);
        row.put("signedCount", signedCount);
        row.put("reviewCount", reviewCount);
        row.put("heatScore", heatScore);
        row.put("heatLabel", resolveHeatLabel(heatScore));
        return row;
    }

    private String resolveHeatLabel(long heatScore) {
        if (heatScore >= 40) {
            return "高热课程";
        }
        if (heatScore >= 18) {
            return "热门课程";
        }
        if (heatScore >= 8) {
            return "持续关注";
        }
        return "体验推荐";
    }
}