package com.graduation.crafttrace.service;

import com.graduation.crafttrace.entity.Course;
import com.graduation.crafttrace.entity.CourseBooking;
import com.graduation.crafttrace.repository.CourseBookingRepository;
import com.graduation.crafttrace.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class BookingService {

    private final CourseRepository courseRepository;
    private final CourseBookingRepository bookingRepository;

    public BookingService(CourseRepository courseRepository,
                          CourseBookingRepository bookingRepository) {
        this.courseRepository = courseRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * 学生报名
     */
    @Transactional
    public CourseBooking book(Long courseId, Long studentUserId) {
        if (courseId == null) throw new ResponseStatusException(BAD_REQUEST, "courseId 不能为空");
        if (studentUserId == null) throw new ResponseStatusException(BAD_REQUEST, "未登录");

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "课程不存在 id=" + courseId));

        if (!"PUBLISHED".equalsIgnoreCase(safe(course.getStatus()))) {
            throw new ResponseStatusException(BAD_REQUEST, "课程未开放报名");
        }

        // 查一次即可：已报名/取消后恢复 都靠这条记录处理
        Optional<CourseBooking> existedOpt = bookingRepository.findByCourseIdAndStudentUserId(courseId, studentUserId);
        if (existedOpt.isPresent()) {
            CourseBooking existed = existedOpt.get();
            // 已经是 BOOKED，拒绝重复
            if ("BOOKED".equalsIgnoreCase(safe(existed.getStatus()))) {
                throw new ResponseStatusException(BAD_REQUEST, "你已报名，无需重复报名");
            }
        }

        // 容量限制（capacity <=0 或 null 表示不限制）
        Integer cap = course.getCapacity();
        if (cap != null && cap > 0) {
            long booked = bookingRepository.countByCourseIdAndStatus(courseId, "BOOKED");
            if (booked >= cap) {
                throw new ResponseStatusException(BAD_REQUEST, "报名人数已满");
            }
        }

        // 之前报过名（CANCELED / ATTENDED）则恢复为 BOOKED；否则新建
        CourseBooking booking = existedOpt.orElseGet(CourseBooking::new);
        booking.setCourseId(courseId);
        booking.setStudentUserId(studentUserId);
        booking.setStatus("BOOKED");

        return bookingRepository.save(booking);
    }

    /**
     * 学生取消报名
     */
    @Transactional
    public CourseBooking cancel(Long courseId, Long studentUserId) {
        if (courseId == null) throw new ResponseStatusException(BAD_REQUEST, "courseId 不能为空");
        if (studentUserId == null) throw new ResponseStatusException(BAD_REQUEST, "未登录");

        CourseBooking booking = bookingRepository.findByCourseIdAndStudentUserId(courseId, studentUserId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "未找到报名记录"));

        if (!"BOOKED".equalsIgnoreCase(safe(booking.getStatus()))) {
            throw new ResponseStatusException(BAD_REQUEST, "当前状态不可取消：" + booking.getStatus());
        }

        booking.setStatus("CANCELED");
        return bookingRepository.save(booking);
    }

    /**
     * 学生：我的报名
     */
    @Transactional(readOnly = true)
    public List<CourseBooking> myBookings(Long studentUserId) {
        if (studentUserId == null) throw new ResponseStatusException(BAD_REQUEST, "未登录");
        return bookingRepository.findByStudentUserIdOrderByBookingTimeDesc(studentUserId);
    }

    /**
     * ✅ 匠人：查看某课程报名列表
     */
    @Transactional(readOnly = true)
    public List<CourseBooking> listByCourse(Long courseId) {
        if (courseId == null) throw new ResponseStatusException(BAD_REQUEST, "courseId 不能为空");
        return bookingRepository.findByCourseIdOrderByBookingTimeDesc(courseId);
    }

    /**
     * ✅ 匠人：签到（BOOKED -> ATTENDED）
     */
    @Transactional
    public CourseBooking attend(Long bookingId) {
        if (bookingId == null) throw new ResponseStatusException(BAD_REQUEST, "bookingId 不能为空");

        CourseBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "报名记录不存在 id=" + bookingId));

        String st = safe(booking.getStatus()).toUpperCase();
        if (!"BOOKED".equals(st)) {
            throw new ResponseStatusException(BAD_REQUEST, "当前状态不可签到：" + booking.getStatus());
        }

        booking.setStatus("ATTENDED");
        return bookingRepository.save(booking);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
