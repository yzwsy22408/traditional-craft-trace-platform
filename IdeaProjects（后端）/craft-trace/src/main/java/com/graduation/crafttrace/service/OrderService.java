package com.graduation.crafttrace.service;

import com.graduation.crafttrace.entity.Course;
import com.graduation.crafttrace.entity.CourseOrder;
import com.graduation.crafttrace.repository.CourseAttendanceRepository;
import com.graduation.crafttrace.repository.CourseOrderRepository;
import com.graduation.crafttrace.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class OrderService {

    private final CourseRepository courseRepository;
    private final CourseOrderRepository orderRepository;
    private final CourseAttendanceRepository attendanceRepository;

    public OrderService(CourseRepository courseRepository,
                        CourseOrderRepository orderRepository,
                        CourseAttendanceRepository attendanceRepository) {
        this.courseRepository = courseRepository;
        this.orderRepository = orderRepository;
        this.attendanceRepository = attendanceRepository;
    }

    /** 创建订单（防重复：同课程同人已有 UNPAID 则复用） */
    @Transactional
    public CourseOrder createOrder(Long courseId, Long studentUserId) {
        if (courseId == null) throw new ResponseStatusException(BAD_REQUEST, "courseId 不能为空");
        if (studentUserId == null) throw new ResponseStatusException(BAD_REQUEST, "未登录");

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "课程不存在 id=" + courseId));

        if (!"PUBLISHED".equalsIgnoreCase(safe(course.getStatus()))) {
            throw new ResponseStatusException(BAD_REQUEST, "课程未开放下单");
        }

        Optional<CourseOrder> existed = orderRepository
                .findTopByCourseIdAndStudentUserIdAndStatusOrderByCreatedAtDesc(courseId, studentUserId, "UNPAID");
        if (existed.isPresent()) return existed.get();

        CourseOrder order = new CourseOrder();
        order.setCourseId(courseId);
        order.setStudentUserId(studentUserId);
        order.setAmount(course.getPrice());
        order.setStatus("UNPAID");

        return orderRepository.save(order);
    }

    /** 支付（模拟）+ ✅ 防超卖：容量 = 已支付人数 */
    @Transactional
    public CourseOrder pay(Long orderId, Long studentUserId) {
        if (orderId == null) throw new ResponseStatusException(BAD_REQUEST, "orderId 不能为空");
        if (studentUserId == null) throw new ResponseStatusException(BAD_REQUEST, "未登录");

        CourseOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "订单不存在 id=" + orderId));

        if (order.getStudentUserId() == null || !order.getStudentUserId().equals(studentUserId)) {
            throw new ResponseStatusException(BAD_REQUEST, "无权限操作该订单");
        }

        String st = safe(order.getStatus()).toUpperCase();
        if (!"UNPAID".equals(st)) {
            throw new ResponseStatusException(BAD_REQUEST, "当前状态不可支付：" + order.getStatus());
        }

        Course course = courseRepository.findById(order.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "课程不存在"));

        if (!"PUBLISHED".equalsIgnoreCase(safe(course.getStatus()))) {
            throw new ResponseStatusException(BAD_REQUEST, "课程未开放支付");
        }

        Integer cap = course.getCapacity();
        if (cap != null && cap > 0) {
            long paidCount = orderRepository.countByCourseIdAndStatus(course.getId(), "PAID");
            if (paidCount >= cap) {
                throw new ResponseStatusException(BAD_REQUEST, "课程名额已满，无法支付");
            }
        }

        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        return orderRepository.save(order);
    }

    /** 取消订单：仅 UNPAID */
    @Transactional
    public CourseOrder cancel(Long orderId, Long studentUserId) {
        if (orderId == null) throw new ResponseStatusException(BAD_REQUEST, "orderId 不能为空");
        if (studentUserId == null) throw new ResponseStatusException(BAD_REQUEST, "未登录");

        CourseOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "订单不存在 id=" + orderId));

        if (order.getStudentUserId() == null || !order.getStudentUserId().equals(studentUserId)) {
            throw new ResponseStatusException(BAD_REQUEST, "无权限操作该订单");
        }

        String st = safe(order.getStatus()).toUpperCase();
        if (!"UNPAID".equals(st)) {
            throw new ResponseStatusException(BAD_REQUEST, "当前状态不可取消：" + order.getStatus());
        }

        order.setStatus("CANCELED");
        return orderRepository.save(order);
    }

    /** ✅ 退款：仅 PAID -> REFUNDED（会释放名额，因为 PAID 统计减少） */
    @Transactional
    public CourseOrder refund(Long orderId, Long studentUserId) {
        if (orderId == null) throw new ResponseStatusException(BAD_REQUEST, "orderId 不能为空");
        if (studentUserId == null) throw new ResponseStatusException(BAD_REQUEST, "未登录");

        CourseOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "订单不存在 id=" + orderId));

        if (order.getStudentUserId() == null || !order.getStudentUserId().equals(studentUserId)) {
            throw new ResponseStatusException(BAD_REQUEST, "无权限操作该订单");
        }

        String st = safe(order.getStatus()).toUpperCase();
        if (!"PAID".equals(st)) {
            throw new ResponseStatusException(BAD_REQUEST, "当前状态不可退款：" + order.getStatus());
        }

        // ✅ 已签到则禁止退款（更合理）
        boolean signed = attendanceRepository.findByCourseIdAndUserId(order.getCourseId(), studentUserId).isPresent();
        if (signed) {
            throw new ResponseStatusException(BAD_REQUEST, "已签到，不能退款");
        }

        order.setStatus("REFUNDED");
        // 你实体里没有 refundedAt 字段，所以不加字段、不改表
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<CourseOrder> myOrders(Long studentUserId) {
        if (studentUserId == null) throw new ResponseStatusException(BAD_REQUEST, "未登录");
        return orderRepository.findByStudentUserIdOrderByCreatedAtDesc(studentUserId);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
