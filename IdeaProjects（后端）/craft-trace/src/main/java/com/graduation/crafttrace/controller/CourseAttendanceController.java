package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.User;
import com.graduation.crafttrace.entity.Course;
import com.graduation.crafttrace.entity.CourseAttendance;
import com.graduation.crafttrace.repository.CourseRepository;
import com.graduation.crafttrace.repository.UserRepository;
import com.graduation.crafttrace.service.AttendanceService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api/attendance")
public class CourseAttendanceController {

    private final AttendanceService service;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public CourseAttendanceController(AttendanceService service,
                                      UserRepository userRepository,
                                      CourseRepository courseRepository) {
        this.service = service;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    /** 学员签到（student） */
    @PostMapping("/sign/{courseId}")
    public CourseAttendance sign(@PathVariable Long courseId, HttpSession session) {
        requireStudent(session);
        Long userId = requireUid(session);
        if (courseId == null) throw new ResponseStatusException(BAD_REQUEST, "courseId 不能为空");
        return service.sign(courseId, userId);
    }

    /** 我的签到（student） */
    @GetMapping("/my")
    public List<CourseAttendance> my(HttpSession session) {
        requireStudent(session);
        Long userId = requireUid(session);
        return service.my(userId);
    }

    /** ✅ 统计（匠人/管理员 + 归属校验） */
    @GetMapping("/course/{courseId}/summary")
    public AttendanceService.AttendanceSummary summary(@PathVariable Long courseId, HttpSession session) {
        ensureHandicraftOrAdmin(session);
        ensureCourseOwnerOrAdmin(session, courseId);
        return service.summary(courseId);
    }

    /** ✅ 签到名单（匠人/管理员 + 归属校验） */
    @GetMapping("/course/{courseId}")
    public List<CourseAttendanceDTO> list(@PathVariable Long courseId, HttpSession session) {
        ensureHandicraftOrAdmin(session);
        ensureCourseOwnerOrAdmin(session, courseId);

        List<CourseAttendance> list = service.listByCourse(courseId);
        if (list == null || list.isEmpty()) return Collections.emptyList();

        Set<Long> userIds = list.stream()
                .map(CourseAttendance::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return list.stream().map(a -> {
            User u = userMap.get(a.getUserId());
            String displayName = pickDisplayName(u, a.getUserId()); // 优先 username，其次 name
            return new CourseAttendanceDTO(
                    a.getId(),
                    a.getCourseId(),
                    a.getUserId(),
                    displayName,
                    safe(u == null ? null : u.getName()),
                    safe(u == null ? null : u.getUsername()),
                    a.getStatus(),
                    a.getSignTime()
            );
        }).collect(Collectors.toList());
    }

    // ===== 权限与工具 =====

    private void requireStudent(HttpSession session) {
        String role = getRole(session);
        if (!"student".equals(role)) {
            throw new ResponseStatusException(FORBIDDEN, "无权限：仅学员可操作");
        }
    }

    private void ensureHandicraftOrAdmin(HttpSession session) {
        String role = getRole(session);
        if (!"handicraft".equals(role) && !"admin".equals(role)) {
            throw new ResponseStatusException(FORBIDDEN, "无权限：仅匠人/管理员可查看");
        }
    }

    /** ✅ 匠人只能查看自己创建的课程；admin 放行 */
    private void ensureCourseOwnerOrAdmin(HttpSession session, Long courseId) {
        if (courseId == null) throw new ResponseStatusException(BAD_REQUEST, "courseId 不能为空");
        String role = getRole(session);
        if ("admin".equals(role)) return;

        Long uid = requireUid(session);
        Course c = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "课程不存在 id=" + courseId));

        if (c.getCreatedBy() == null || !c.getCreatedBy().equals(uid)) {
            throw new ResponseStatusException(FORBIDDEN, "无权限：只能查看自己创建课程的签到名单/统计");
        }
    }

    private Long requireUid(HttpSession session) {
        Object uid = session.getAttribute("LOGIN_USER_ID");
        if (uid == null) throw new ResponseStatusException(FORBIDDEN, "未登录或登录已过期");
        return Long.valueOf(uid.toString());
    }

    private String getRole(HttpSession session) {
        Object roleObj = session.getAttribute("LOGIN_ROLE");
        return roleObj == null ? "" : roleObj.toString();
    }

    /** ✅ 优先 username，其次 name，再兜底 用户#id */
    private String pickDisplayName(User u, Long userId) {
        if (u == null) return "用户#" + (userId == null ? "?" : userId);
        String username = safe(u.getUsername());
        if (!username.isEmpty()) return username;
        String name = safe(u.getName());
        if (!name.isEmpty()) return name;
        return "用户#" + (u.getId() == null ? "?" : u.getId());
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    public static class CourseAttendanceDTO {
        private Long id;
        private Long courseId;
        private Long userId;

        private String studentName;
        private String name;
        private String username;

        private String status;
        private Object signTime;

        public CourseAttendanceDTO() {}

        public CourseAttendanceDTO(Long id, Long courseId, Long userId,
                                   String studentName, String name, String username,
                                   String status, Object signTime) {
            this.id = id;
            this.courseId = courseId;
            this.userId = userId;
            this.studentName = studentName;
            this.name = name;
            this.username = username;
            this.status = status;
            this.signTime = signTime;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Object getSignTime() { return signTime; }
        public void setSignTime(Object signTime) { this.signTime = signTime; }
    }
}
