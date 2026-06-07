package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.User;
import com.graduation.crafttrace.entity.Course;
import com.graduation.crafttrace.entity.CourseReview;
import com.graduation.crafttrace.common.Result;
import com.graduation.crafttrace.repository.CourseRepository;
import com.graduation.crafttrace.repository.UserRepository;
import com.graduation.crafttrace.repository.CourseReviewRepository;
import com.graduation.crafttrace.service.CourseReviewService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/review")
public class CourseReviewController {

    private final CourseReviewService service;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseReviewRepository courseReviewRepository;

    public CourseReviewController(CourseReviewService service,
                                  CourseRepository courseRepository,
                                  UserRepository userRepository,
                                  CourseReviewRepository courseReviewRepository) {
        this.service = service;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.courseReviewRepository = courseReviewRepository;
    }

    /**
     * ✅ 核心修复：管理端获取评价列表
     * 1. 匠人（handicraft）仅能看到自己名下课程的评价
     * 2. 管理员（admin）可查看全量数据
     * 3. 补全了回复人（replyArtisanName）信息
     */
    @GetMapping("/admin/list")
    public Result listAdmin(HttpSession session) {
        // 鉴权：获取当前登录者ID和角色
        Object uidObj = session.getAttribute("LOGIN_USER_ID");
        Object roleObj = session.getAttribute("LOGIN_ROLE");
        if (uidObj == null) return Result.error("未登录或登录已过期");

        Long currentUid = Long.valueOf(uidObj.toString());
        String role = String.valueOf(roleObj);

        List<CourseReview> rawList;
        if ("admin".equals(role)) {
            // 管理员看全量
            rawList = courseReviewRepository.findAll();
        } else if ("handicraft".equals(role)) {
            // 匠人：只看自己课程的评价
            List<Course> myCourses = courseRepository.findByCreatedByOrderByCreatedAtDesc(currentUid);
            List<Long> myCourseIds = myCourses.stream().map(Course::getId).collect(Collectors.toList());
            if (myCourseIds.isEmpty()) {
                Result empty = Result.ok("暂无评价");
                empty.setData(Collections.emptyList());
                return empty;
            }
            // 过滤出属于该匠人课程的评价
            rawList = courseReviewRepository.findAll().stream()
                    .filter(r -> myCourseIds.contains(r.getCourseId()))
                    .collect(Collectors.toList());
        } else {
            return Result.error("权限不足");
        }

        if (rawList.isEmpty()) {
            Result emptyRes = Result.ok("暂无评价");
            emptyRes.setData(Collections.emptyList());
            return emptyRes;
        }

        // 批量提取相关ID进行名称补全
        Set<Long> studentIds = rawList.stream().map(CourseReview::getStudentUserId).collect(Collectors.toSet());
        Set<Long> courseIds = rawList.stream().map(CourseReview::getCourseId).collect(Collectors.toSet());
        Set<Long> artisanIds = rawList.stream().map(CourseReview::getReplyArtisanId).filter(Objects::nonNull).collect(Collectors.toSet());

        Set<Long> allUserIds = new HashSet<>(studentIds);
        allUserIds.addAll(artisanIds);

        Map<Long, String> userNames = userRepository.findAllById(allUserIds).stream()
                .collect(Collectors.toMap(User::getId, this::pickDisplayName));

        Map<Long, String> courseTitles = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Course::getTitle));

        // 封装返回数据
        List<Map<String, Object>> resList = rawList.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("studentName", userNames.getOrDefault(r.getStudentUserId(), "未知学员"));
            m.put("courseName", courseTitles.getOrDefault(r.getCourseId(), "未知课程"));
            m.put("rating", r.getRating());
            m.put("content", r.getContent() != null ? r.getContent().trim() : "-");
            m.put("reply", r.getReply());
            // ✅ 关键：显示谁回复的
            m.put("replyArtisanName", r.getReplyArtisanId() != null ? userNames.get(r.getReplyArtisanId()) : null);
            m.put("createdAt", r.getCreatedAt());
            return m;
        }).sorted((a, b) -> ((LocalDateTime)b.get("createdAt")).compareTo((LocalDateTime)a.get("createdAt")))
                .collect(Collectors.toList());

        Result finalRes = Result.ok("获取成功");
        finalRes.setData(resList);
        return finalRes;
    }

    /** 匠人/管理员：回复评价 */
    @PostMapping("/reply/{id}")
    public Result reply(@PathVariable Long id, @RequestBody Map<String, String> body, HttpSession session) {
        Object uidObj = session.getAttribute("LOGIN_USER_ID");
        if (uidObj == null) return Result.error("未登录");

        Optional<CourseReview> opt = courseReviewRepository.findById(id);
        if (opt.isEmpty()) return Result.error("评价不存在");

        CourseReview review = opt.get();
        review.setReply(body.get("reply"));
        // ✅ 记录回复人的ID，用于之后显示“谁回复的”
        review.setReplyArtisanId(Long.valueOf(uidObj.toString()));
        courseReviewRepository.save(review);
        return Result.ok("回复成功");
    }

    /** 课程详情页展示接口 */
    @GetMapping("/course/{courseId}")
    public List<CourseReviewDTO> list(@PathVariable Long courseId) {
        List<CourseReview> list = service.listByCourse(courseId);
        if (list == null || list.isEmpty()) return Collections.emptyList();
        Set<Long> uids = list.stream().map(CourseReview::getStudentUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(uids).stream().collect(Collectors.toMap(User::getId, u -> u));
        return list.stream().map(r -> {
            User u = userMap.get(r.getStudentUserId());
            String studentName = pickDisplayName(u, r.getStudentUserId());
            return new CourseReviewDTO(r.getId(), r.getCourseId(), r.getStudentUserId(), studentName,
                    r.getRating(), r.getContent(), r.getReply(), r.getCreatedAt(), r.getUpdatedAt());
        }).collect(Collectors.toList());
    }

    private String pickDisplayName(User user) {
        return pickDisplayName(user, user == null ? null : user.getId());
    }

    private String pickDisplayName(User user, Long fallbackId) {
        if (user != null) {
            if (user.getName() != null && !user.getName().trim().isEmpty()) {
                return user.getName().trim();
            }
            if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                return user.getUsername().trim();
            }
        }
        return fallbackId == null ? "用户" : "用户#" + fallbackId;
    }

    @GetMapping("/my-reviews-list")
    public Result getMyReviews(HttpSession session) {
        Object uidObj = session.getAttribute("LOGIN_USER_ID");
        if (uidObj == null) return Result.error("未登录");
        List<CourseReview> list = courseReviewRepository.findByStudentUserIdOrderByCreatedAtDesc(Long.valueOf(uidObj.toString()));
        Result res = Result.ok("OK");
        res.setData(list);
        return res;
    }

    @PostMapping("/course/{courseId}")
    public CourseReview upsert(@PathVariable Long courseId, @RequestBody ReviewReq req, HttpSession session) {
        Object uid = session.getAttribute("LOGIN_USER_ID");
        return service.upsert(courseId, Long.valueOf(uid.toString()), req.getRating(), req.getContent());
    }

    // ===== DTO & Inner Classes =====
    public static class ReviewReq {
        private Integer rating; private String content;
        public Integer getRating() { return rating; } public void setRating(Integer rating) { this.rating = rating; }
        public String getContent() { return content; } public void setContent(String content) { this.content = content; }
    }

    public static class CourseReviewDTO {
        private Long id; private Long courseId; private Long studentUserId; private String studentName;
        private Integer rating; private String content; private String reply; private Object createdAt; private Object updatedAt;
        public CourseReviewDTO() {}
        public CourseReviewDTO(Long id, Long courseId, Long studentUserId, String studentName, Integer rating, String content, String reply, Object createdAt, Object updatedAt) {
            this.id = id; this.courseId = courseId; this.studentUserId = studentUserId; this.studentName = studentName; this.rating = rating; this.content = content; this.reply = reply; this.createdAt = createdAt; this.updatedAt = updatedAt;
        }
        public Long getId() { return id; } public void setId(Long id) { this.id = id; }
        public Long getCourseId() { return courseId; } public void setCourseId(Long courseId) { this.courseId = courseId; }
        public Long getStudentUserId() { return studentUserId; } public void setStudentUserId(Long studentUserId) { this.studentUserId = studentUserId; }
        public String getStudentName() { return studentName; } public void setStudentName(String studentName) { this.studentName = studentName; }
        public Integer getRating() { return rating; } public void setRating(Integer rating) { this.rating = rating; }
        public String getContent() { return content; } public void setContent(String content) { this.content = content; }
        public String getReply() { return reply; } public void setReply(String reply) { this.reply = reply; }
        public Object getCreatedAt() { return createdAt; } public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }
        public Object getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Object updatedAt) { this.updatedAt = updatedAt; }
    }
}
