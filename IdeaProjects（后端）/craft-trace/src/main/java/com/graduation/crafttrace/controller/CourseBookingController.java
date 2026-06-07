package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.User;
import com.graduation.crafttrace.entity.Course;
import com.graduation.crafttrace.entity.CourseBooking;
import com.graduation.crafttrace.repository.CourseRepository;
import com.graduation.crafttrace.repository.UserRepository;
import com.graduation.crafttrace.service.BookingService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api/booking")
public class CourseBookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public CourseBookingController(BookingService bookingService,
                                   UserRepository userRepository,
                                   CourseRepository courseRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    /** 学生：报名 */
    @PostMapping("/{courseId}")
    public CourseBooking book(@PathVariable Long courseId, HttpSession session) {
        requireStudent(session);
        Long uid = requireUid(session);
        if (courseId == null) throw new ResponseStatusException(BAD_REQUEST, "courseId 不能为空");
        return bookingService.book(courseId, uid);
    }

    /** 学生：取消报名 */
    @PostMapping("/{courseId}/cancel")
    public CourseBooking cancel(@PathVariable Long courseId, HttpSession session) {
        requireStudent(session);
        Long uid = requireUid(session);
        if (courseId == null) throw new ResponseStatusException(BAD_REQUEST, "courseId 不能为空");
        return bookingService.cancel(courseId, uid);
    }

    /** 学生：我的报名 */
    @GetMapping("/my")
    public List<CourseBooking> my(HttpSession session) {
        requireStudent(session);
        Long uid = requireUid(session);
        return bookingService.myBookings(uid);
    }

    /**
     * ✅ 匠人/管理员：查看某课程报名名单
     * ✅ 匠人只能查看自己创建的课程
     */
    @GetMapping("/course/{courseId}")
    public List<CourseBookingDTO> listByCourse(@PathVariable Long courseId, HttpSession session) {
        ensureHandicraftOrAdmin(session);
        ensureCourseOwnerOrAdmin(session, courseId);

        if (courseId == null) throw new ResponseStatusException(BAD_REQUEST, "courseId 不能为空");

        List<CourseBooking> list = bookingService.listByCourse(courseId);
        if (list == null || list.isEmpty()) return Collections.emptyList();

        Set<Long> userIds = list.stream()
                .map(CourseBooking::getStudentUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return list.stream().map(b -> {
            User u = userMap.get(b.getStudentUserId());
            String displayName = pickDisplayName(u, b.getStudentUserId()); // 优先 username
            return new CourseBookingDTO(
                    b.getId(),
                    b.getCourseId(),
                    b.getStudentUserId(),
                    displayName,
                    safe(u == null ? null : u.getName()),
                    safe(u == null ? null : u.getUsername()),
                    b.getStatus(),
                    b.getBookingTime()
            );
        }).collect(Collectors.toList());
    }

    /**
     * ✅ 匠人/管理员：把某条报名改为 ATTENDED
     * ✅ 匠人只能操作自己课程的报名记录
     */
    @PostMapping("/{bookingId}/attend")
    public CourseBooking attend(@PathVariable Long bookingId, HttpSession session) {
        ensureHandicraftOrAdmin(session);
        if (bookingId == null) throw new ResponseStatusException(BAD_REQUEST, "bookingId 不能为空");

        CourseBooking b = bookingService.attend(bookingId); // 先执行状态校验
        // 再做“归属校验”：报名记录的 courseId 必须属于当前匠人
        ensureCourseOwnerOrAdmin(session, b.getCourseId());
        return b;
    }

    // ===== 权限与工具方法 =====

    private void requireStudent(HttpSession session) {
        String role = getRole(session);
        if (!"student".equals(role)) {
            throw new ResponseStatusException(FORBIDDEN, "无权限：仅学员可操作");
        }
    }

    private void ensureHandicraftOrAdmin(HttpSession session) {
        String role = getRole(session);
        if (!"handicraft".equals(role) && !"admin".equals(role)) {
            throw new ResponseStatusException(FORBIDDEN, "无权限：仅匠人/管理员可操作");
        }
    }

    /** ✅ 匠人只能看自己创建的课程；admin 放行 */
    private void ensureCourseOwnerOrAdmin(HttpSession session, Long courseId) {
        if (courseId == null) throw new ResponseStatusException(BAD_REQUEST, "courseId 不能为空");
        String role = getRole(session);
        if ("admin".equals(role)) return;

        Long uid = requireUid(session);
        Course c = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "课程不存在 id=" + courseId));

        if (c.getCreatedBy() == null || !c.getCreatedBy().equals(uid)) {
            throw new ResponseStatusException(FORBIDDEN, "无权限：只能查看/操作自己创建课程的名单");
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

    public static class CourseBookingDTO {
        private Long id;
        private Long courseId;
        private Long studentUserId;

        private String studentName;
        private String name;
        private String username;

        private String status;
        private Object bookingTime;

        public CourseBookingDTO() {}

        public CourseBookingDTO(Long id, Long courseId, Long studentUserId,
                                String studentName, String name, String username,
                                String status, Object bookingTime) {
            this.id = id;
            this.courseId = courseId;
            this.studentUserId = studentUserId;
            this.studentName = studentName;
            this.name = name;
            this.username = username;
            this.status = status;
            this.bookingTime = bookingTime;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }

        public Long getStudentUserId() { return studentUserId; }
        public void setStudentUserId(Long studentUserId) { this.studentUserId = studentUserId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Object getBookingTime() { return bookingTime; }
        public void setBookingTime(Object bookingTime) { this.bookingTime = bookingTime; }
    }
}
