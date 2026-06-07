package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.User;
import com.graduation.crafttrace.common.Result;
import com.graduation.crafttrace.entity.Course;
import com.graduation.crafttrace.entity.CourseAttendance;
import com.graduation.crafttrace.entity.StudentWork;
import com.graduation.crafttrace.repository.CourseAttendanceRepository;
import com.graduation.crafttrace.repository.CourseRepository;
import com.graduation.crafttrace.repository.StudentWorkRepository;
import com.graduation.crafttrace.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api/student-work")
public class StudentWorkController {

    private final StudentWorkRepository studentWorkRepository;
    private final CourseRepository courseRepository;
    private final CourseAttendanceRepository courseAttendanceRepository;
    private final UserRepository userRepository;

    public StudentWorkController(
            StudentWorkRepository studentWorkRepository,
            CourseRepository courseRepository,
            CourseAttendanceRepository courseAttendanceRepository,
            UserRepository userRepository
    ) {
        this.studentWorkRepository = studentWorkRepository;
        this.courseRepository = courseRepository;
        this.courseAttendanceRepository = courseAttendanceRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/my")
    public Result my(HttpSession session) {
        Long studentUserId = requireStudentUid(session);
        List<StudentWork> works = studentWorkRepository.findByStudentUserIdOrderByUpdatedAtDesc(studentUserId);
        Result result = Result.ok("获取成功");
        result.setData(enrichWorks(works));
        return result;
    }

    @GetMapping("/course/{courseId}")
    public Result listByCourse(@PathVariable Long courseId, HttpSession session) {
        requireLogin(session);
        if (courseId == null) {
            throw new ResponseStatusException(BAD_REQUEST, "课程编号不能为空");
        }
        List<StudentWork> works = studentWorkRepository.findByCourseIdOrderByUpdatedAtDesc(courseId);
        Result result = Result.ok("获取成功");
        result.setData(enrichWorks(works));
        return result;
    }

    @PostMapping
    public Result createOrUpdate(@RequestBody StudentWorkReq req, HttpSession session) {
        Long studentUserId = requireStudentUid(session);
        validateReq(req);
        ensureSignedCourse(req.getCourseId(), studentUserId);

        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "未找到对应课程"));

        StudentWork work = studentWorkRepository.findByCourseIdAndStudentUserId(req.getCourseId(), studentUserId)
                .orElseGet(StudentWork::new);

        work.setCourseId(course.getId());
        work.setStudentUserId(studentUserId);
        work.setWorkTitle(req.getWorkTitle());
        work.setImageUrl(req.getImageUrl());
        work.setReflection(req.getReflection());
        work.setGainText(req.getGainText());

        StudentWork saved = studentWorkRepository.save(work);
        Result result = Result.ok("作品展示已保存");
        result.setData(enrichWorks(Collections.singletonList(saved)).stream().findFirst().orElse(null));
        return result;
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody StudentWorkReq req, HttpSession session) {
        Long studentUserId = requireStudentUid(session);
        if (id == null) {
            throw new ResponseStatusException(BAD_REQUEST, "作品编号不能为空");
        }
        validateReq(req);

        StudentWork work = studentWorkRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "未找到对应作品记录"));
        if (!studentUserId.equals(work.getStudentUserId())) {
            throw new ResponseStatusException(FORBIDDEN, "只能修改自己的作品展示");
        }

        ensureSignedCourse(req.getCourseId(), studentUserId);
        work.setCourseId(req.getCourseId());
        work.setWorkTitle(req.getWorkTitle());
        work.setImageUrl(req.getImageUrl());
        work.setReflection(req.getReflection());
        work.setGainText(req.getGainText());

        StudentWork saved = studentWorkRepository.save(work);
        Result result = Result.ok("作品展示已更新");
        result.setData(enrichWorks(Collections.singletonList(saved)).stream().findFirst().orElse(null));
        return result;
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id, HttpSession session) {
        Long studentUserId = requireStudentUid(session);
        if (id == null) {
            throw new ResponseStatusException(BAD_REQUEST, "作品编号不能为空");
        }

        StudentWork work = studentWorkRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "未找到对应作品记录"));
        if (!studentUserId.equals(work.getStudentUserId())) {
            throw new ResponseStatusException(FORBIDDEN, "只能删除自己的作品展示");
        }

        studentWorkRepository.deleteById(id);
        return Result.ok("作品展示已删除");
    }

    private List<Map<String, Object>> enrichWorks(List<StudentWork> works) {
        if (works == null || works.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> courseIds = works.stream().map(StudentWork::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> userIds = works.stream().map(StudentWork::getStudentUserId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Course::getTitle));
        Map<Long, String> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, this::pickDisplayName));

        return works.stream().map(work -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", work.getId());
            row.put("courseId", work.getCourseId());
            row.put("courseTitle", courseMap.getOrDefault(work.getCourseId(), "课程作品"));
            row.put("studentUserId", work.getStudentUserId());
            row.put("studentName", userMap.getOrDefault(work.getStudentUserId(), "学员"));
            row.put("workTitle", work.getWorkTitle());
            row.put("imageUrl", work.getImageUrl());
            row.put("reflection", work.getReflection());
            row.put("gainText", work.getGainText());
            row.put("createdAt", work.getCreatedAt());
            row.put("updatedAt", work.getUpdatedAt());
            return row;
        }).collect(Collectors.toList());
    }

    private void validateReq(StudentWorkReq req) {
        if (req == null) {
            throw new ResponseStatusException(BAD_REQUEST, "请求体不能为空");
        }
        if (req.getCourseId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "请选择所属课程");
        }
        if (isBlank(req.getWorkTitle())) {
            throw new ResponseStatusException(BAD_REQUEST, "作品标题不能为空");
        }
        if (isBlank(req.getImageUrl())) {
            throw new ResponseStatusException(BAD_REQUEST, "请先上传作品照片");
        }
    }

    private void ensureSignedCourse(Long courseId, Long studentUserId) {
        CourseAttendance attendance = courseAttendanceRepository.findByCourseIdAndUserId(courseId, studentUserId)
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "完成签到后才可上传课程作品"));
        if (!"SIGNED".equalsIgnoreCase(String.valueOf(attendance.getStatus()))) {
            throw new ResponseStatusException(FORBIDDEN, "完成签到后才可上传课程作品");
        }
    }

    private Long requireStudentUid(HttpSession session) {
        String role = getRole(session);
        if (!"student".equals(role)) {
            throw new ResponseStatusException(FORBIDDEN, "仅学生可维护个人作品展示");
        }
        return requireLogin(session);
    }

    private Long requireLogin(HttpSession session) {
        Object uid = session.getAttribute("LOGIN_USER_ID");
        if (uid == null) {
            throw new ResponseStatusException(FORBIDDEN, "请先登录");
        }
        return Long.valueOf(uid.toString());
    }

    private String getRole(HttpSession session) {
        Object roleObj = session.getAttribute("LOGIN_ROLE");
        return roleObj == null ? "" : String.valueOf(roleObj);
    }

    private String pickDisplayName(User user) {
        if (user == null) {
            return "学员";
        }
        if (!isBlank(user.getName())) {
            return user.getName().trim();
        }
        if (!isBlank(user.getUsername())) {
            return user.getUsername().trim();
        }
        return "学员#" + user.getId();
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static class StudentWorkReq {
        private Long courseId;
        private String workTitle;
        private String imageUrl;
        private String reflection;
        private String gainText;

        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }

        public String getWorkTitle() {
            return workTitle;
        }

        public void setWorkTitle(String workTitle) {
            this.workTitle = workTitle;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getReflection() {
            return reflection;
        }

        public void setReflection(String reflection) {
            this.reflection = reflection;
        }

        public String getGainText() {
            return gainText;
        }

        public void setGainText(String gainText) {
            this.gainText = gainText;
        }
    }
}