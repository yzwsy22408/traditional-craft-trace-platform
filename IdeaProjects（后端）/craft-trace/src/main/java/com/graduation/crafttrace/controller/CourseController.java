package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.entity.Course;
import com.graduation.crafttrace.repository.CourseRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    private final CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @GetMapping
    public List<Course> listAll(HttpSession session) {
        return courseRepository.findAll();
    }

    @GetMapping("/mine")
    public List<Course> mine(HttpSession session) {
        ensureHandicraftOrAdmin(session);
        Long uid = requireUid(session);
        return courseRepository.findByCreatedByOrderByCreatedAtDesc(uid);
    }

    @PostMapping
    public Course create(@RequestBody Course c, HttpSession session) {
        ensureHandicraftOrAdmin(session);
        if (c == null) throw new ResponseStatusException(BAD_REQUEST, "request body can not be null");
        if (isBlank(c.getTitle())) throw new ResponseStatusException(BAD_REQUEST, "title can not be empty");

        Long uid = requireUid(session);
        c.setCreatedBy(uid);

        if (isBlank(c.getStatus())) c.setStatus("DRAFT");
        if (c.getCapacity() == null) c.setCapacity(0);
        normalizeTeacherFields(c);
        assertCourseStatus(c.getStatus());
        validateTimeRange(c);
        c.setTitle(c.getTitle().trim());
        return courseRepository.save(c);
    }

    @PutMapping("/{id}")
    public Course update(@PathVariable Long id, @RequestBody Course req, HttpSession session) {
        ensureHandicraftOrAdmin(session);
        Course c = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "course not found: " + id));

        ensureOwnerOrAdmin(session, c);

        if (!isBlank(req.getTitle())) c.setTitle(req.getTitle().trim());
        if (req.getIntro() != null) c.setIntro(req.getIntro());
        if (req.getCoverUrl() != null) c.setCoverUrl(req.getCoverUrl());
        if (req.getPrice() != null) c.setPrice(req.getPrice());
        if (req.getCapacity() != null) c.setCapacity(req.getCapacity());
        if (req.getStartTime() != null) c.setStartTime(req.getStartTime());
        if (req.getEndTime() != null) c.setEndTime(req.getEndTime());
        if (req.getWorkshopId() != null) c.setWorkshopId(req.getWorkshopId());
        if (req.getCategory() != null) c.setCategory(req.getCategory().trim());

        c.setTeacherId(req.getTeacherId());
        c.setTeacherName(req.getTeacherName());
        c.setTeacherSchoolName(req.getTeacherSchoolName());
        c.setTeacherSubjectName(req.getTeacherSubjectName());
        c.setLeadRoute(req.getLeadRoute());
        c.setTeacherNote(req.getTeacherNote());
        normalizeTeacherFields(c);

        if (req.getStatus() != null) {
            String st = req.getStatus().trim().toUpperCase();
            assertCourseStatus(st);
            c.setStatus(st);
        }

        validateTimeRange(c);
        return courseRepository.save(c);
    }

    @PostMapping("/{id}/publish")
    public Course publish(@PathVariable Long id, HttpSession session) {
        ensureHandicraftOrAdmin(session);
        Course c = courseRepository.findById(id).orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "course not found"));
        ensureOwnerOrAdmin(session, c);
        c.setStatus("PUBLISHED");
        return courseRepository.save(c);
    }

    @PostMapping("/{id}/close")
    public Course close(@PathVariable Long id, HttpSession session) {
        ensureHandicraftOrAdmin(session);
        Course c = courseRepository.findById(id).orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "course not found"));
        ensureOwnerOrAdmin(session, c);
        c.setStatus("CLOSED");
        return courseRepository.save(c);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        ensureHandicraftOrAdmin(session);
        Course c = courseRepository.findById(id).orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "course not found"));
        ensureOwnerOrAdmin(session, c);
        if (!"DRAFT".equals(c.getStatus())) throw new ResponseStatusException(BAD_REQUEST, "only draft can be deleted");
        courseRepository.deleteById(id);
        return "OK";
    }

    @GetMapping("/public/courses")
    public List<Course> publicList() {
        return courseRepository.findAll().stream()
                .filter(c -> "PUBLISHED".equals(c.getStatus()))
                .collect(Collectors.toList());
    }

    @GetMapping("/public/courses/{id}")
    public Course publicDetail(@PathVariable Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "course not found"));
    }

    private void ensureHandicraftOrAdmin(HttpSession session) {
        Object roleObj = session.getAttribute("LOGIN_ROLE");
        String role = roleObj == null ? null : roleObj.toString();
        if (!"handicraft".equals(role) && !"admin".equals(role)) {
            throw new ResponseStatusException(FORBIDDEN, "forbidden");
        }
    }

    private void ensureOwnerOrAdmin(HttpSession session, Course c) {
        Long uid = requireUid(session);
        Object roleObj = session.getAttribute("LOGIN_ROLE");
        if ("admin".equals(roleObj)) return;
        if (c.getCreatedBy() == null || !c.getCreatedBy().equals(uid)) {
            throw new ResponseStatusException(FORBIDDEN, "forbidden");
        }
    }

    private Long requireUid(HttpSession session) {
        Object uid = session.getAttribute("LOGIN_USER_ID");
        if (uid == null) throw new ResponseStatusException(FORBIDDEN, "not login");
        return Long.valueOf(uid.toString());
    }

    private void assertCourseStatus(String status) {
        String st = (status == null) ? "" : status.trim().toUpperCase();
        if (!("DRAFT".equals(st) || "PUBLISHED".equals(st) || "CLOSED".equals(st))) {
            throw new ResponseStatusException(BAD_REQUEST, "invalid status");
        }
    }

    private void validateTimeRange(Course c) {
        if (c.getStartTime() != null && c.getEndTime() != null && c.getEndTime().isBefore(c.getStartTime())) {
            throw new ResponseStatusException(BAD_REQUEST, "endTime can not be before startTime");
        }
    }

    private void normalizeTeacherFields(Course c) {
        c.setTeacherName(trimToNull(c.getTeacherName()));
        c.setTeacherSchoolName(trimToNull(c.getTeacherSchoolName()));
        c.setTeacherSubjectName(trimToNull(c.getTeacherSubjectName()));
        c.setLeadRoute(trimToNull(c.getLeadRoute()));
        c.setTeacherNote(trimToNull(c.getTeacherNote()));
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
