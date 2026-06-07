package com.graduation.crafttrace.service;

import com.graduation.crafttrace.entity.CourseAttendance;
import com.graduation.crafttrace.entity.CourseBooking;
import com.graduation.crafttrace.entity.CourseOrder;
import com.graduation.crafttrace.repository.CourseAttendanceRepository;
import com.graduation.crafttrace.repository.CourseBookingRepository;
import com.graduation.crafttrace.repository.CourseOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final CourseAttendanceRepository attendanceRepo;
    private final CourseBookingRepository bookingRepo;
    private final CourseOrderRepository orderRepo;

    public AttendanceService(
            CourseAttendanceRepository attendanceRepo,
            CourseBookingRepository bookingRepo,
            CourseOrderRepository orderRepo
    ) {
        this.attendanceRepo = attendanceRepo;
        this.bookingRepo = bookingRepo;
        this.orderRepo = orderRepo;
    }

    /** 学员签到（唯一入口）：已报名 + 已支付 + 未签到 */
    public CourseAttendance sign(Long courseId, Long userId) {

        // ① 已报名
        CourseBooking booking = bookingRepo
                .findByCourseIdAndStudentUserId(courseId, userId)
                .orElseThrow(() -> new RuntimeException("未报名，不能签到"));

        if (!"BOOKED".equals(booking.getStatus())) {
            throw new RuntimeException("报名状态异常，不能签到");
        }

        // ② 已支付
        orderRepo.findTopByCourseIdAndStudentUserIdAndStatusOrderByCreatedAtDesc(
                courseId, userId, "PAID"
        ).orElseThrow(() -> new RuntimeException("未支付课程费用，不能签到"));

        // ③ 未签到过
        if (attendanceRepo.findByCourseIdAndUserId(courseId, userId).isPresent()) {
            throw new RuntimeException("已签到，不能重复签到");
        }

        // ④ 执行签到
        CourseAttendance a = new CourseAttendance();
        a.setCourseId(courseId);
        a.setUserId(userId);
        a.setStatus("SIGNED");
        a.setSignTime(LocalDateTime.now());

        return attendanceRepo.save(a);
    }

    /** 我的签到 */
    public List<CourseAttendance> my(Long userId) {
        return attendanceRepo.findByUserId(userId);
    }

    /** 某课程签到列表（匠人/老师） */
    public List<CourseAttendance> listByCourse(Long courseId) {
        return attendanceRepo.findByCourseId(courseId);
    }

    /**
     * ✅ 统计口径（都按“人数去重”）
     * - 报名：BOOKED 的人数
     * - 已支付：同时满足 BOOKED 且有 PAID 订单的人数
     * - 已签到：SIGNED 的人数
     * - 实到率：已签到 / 已支付
     */
    public AttendanceSummary summary(Long courseId) {

        // 报名（只算 BOOKED）
        long booked = bookingRepo.countDistinctStudentUserIdByCourseIdAndStatus(courseId, "BOOKED");

        // 已支付（必须“报名BOOKED + 支付PAID”交集）
        List<CourseOrder> paidOrders = orderRepo.findByCourseIdAndStatus(courseId, "PAID");
        Set<Long> paidUserIds = (paidOrders == null ? Collections.<CourseOrder>emptyList() : paidOrders)
                .stream()
                .map(CourseOrder::getStudentUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        long paid = paidUserIds.stream().filter(uid -> {
            return bookingRepo.findByCourseIdAndStudentUserId(courseId, uid)
                    .map(b -> "BOOKED".equals(b.getStatus()))
                    .orElse(false);
        }).count();

        // 已签到（SIGNED 人数）
        long signed = attendanceRepo.countDistinctUserIdByCourseIdAndStatus(courseId, "SIGNED");

        double rate = 0.0;
        if (paid > 0) rate = (signed * 1.0 / paid);

        AttendanceSummary s = new AttendanceSummary();
        s.setCourseId(courseId);
        s.setBookedCount(booked);
        s.setPaidCount(paid);
        s.setSignedCount(signed);
        s.setAttendanceRate(rate);
        return s;
    }

    public static class AttendanceSummary {
        private Long courseId;
        private Long bookedCount;
        private Long paidCount;
        private Long signedCount;
        private Double attendanceRate;

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }

        public Long getBookedCount() { return bookedCount; }
        public void setBookedCount(Long bookedCount) { this.bookedCount = bookedCount; }

        public Long getPaidCount() { return paidCount; }
        public void setPaidCount(Long paidCount) { this.paidCount = paidCount; }

        public Long getSignedCount() { return signedCount; }
        public void setSignedCount(Long signedCount) { this.signedCount = signedCount; }

        public Double getAttendanceRate() { return attendanceRate; }
        public void setAttendanceRate(Double attendanceRate) { this.attendanceRate = attendanceRate; }
    }
}
