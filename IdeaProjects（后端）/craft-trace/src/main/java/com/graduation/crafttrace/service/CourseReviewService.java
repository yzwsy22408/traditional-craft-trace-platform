package com.graduation.crafttrace.service;

import com.graduation.crafttrace.entity.CourseReview;
import com.graduation.crafttrace.repository.CourseAttendanceRepository;
import com.graduation.crafttrace.repository.CourseReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class CourseReviewService {

    private final CourseReviewRepository reviewRepo;
    private final CourseAttendanceRepository attendanceRepo;

    public CourseReviewService(CourseReviewRepository reviewRepo,
                               CourseAttendanceRepository attendanceRepo) {
        this.reviewRepo = reviewRepo;
        this.attendanceRepo = attendanceRepo;
    }

    /** 学员：提交/更新评价（必须已签到） */
    @Transactional
    public CourseReview upsert(Long courseId, Long userId, Integer rating, String content) {
        if (courseId == null) throw new ResponseStatusException(BAD_REQUEST, "courseId 不能为空");
        if (userId == null) throw new ResponseStatusException(BAD_REQUEST, "未登录");
        if (rating == null || rating < 1 || rating > 5) {
            throw new ResponseStatusException(BAD_REQUEST, "评分必须是 1~5");
        }

        // ✅ 必须已签到
        boolean signed = attendanceRepo.findByCourseIdAndUserId(courseId, userId).isPresent();
        if (!signed) {
            throw new ResponseStatusException(BAD_REQUEST, "未签到，不能评价");
        }

        CourseReview r = reviewRepo.findByCourseIdAndStudentUserId(courseId, userId)
                .orElseGet(CourseReview::new);

        r.setCourseId(courseId);
        r.setStudentUserId(userId);
        r.setRating(rating);
        r.setContent(content);

        return reviewRepo.save(r);
    }

    /** 学员：我对某课程的评价 */
    @Transactional(readOnly = true)
    public CourseReview myReview(Long courseId, Long userId) {
        if (courseId == null) throw new ResponseStatusException(BAD_REQUEST, "courseId 不能为空");
        if (userId == null) throw new ResponseStatusException(BAD_REQUEST, "未登录");
        return reviewRepo.findByCourseIdAndStudentUserId(courseId, userId).orElse(null);
    }

    /** 匠人/管理员：某课程评价列表 */
    @Transactional(readOnly = true)
    public List<CourseReview> listByCourse(Long courseId) {
        if (courseId == null) throw new ResponseStatusException(BAD_REQUEST, "courseId 不能为空");
        return reviewRepo.findByCourseIdOrderByCreatedAtDesc(courseId);
    }

    /** 学员：我的评价列表 */
    @Transactional(readOnly = true)
    public List<CourseReview> myList(Long userId) {
        if (userId == null) throw new ResponseStatusException(BAD_REQUEST, "未登录");
        return reviewRepo.findByStudentUserIdOrderByCreatedAtDesc(userId);
    }
}
