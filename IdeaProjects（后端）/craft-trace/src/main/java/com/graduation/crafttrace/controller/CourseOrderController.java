package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.entity.CourseOrder;
import com.graduation.crafttrace.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class CourseOrderController {

    private final OrderService orderService;

    public CourseOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 下单
    @PostMapping("/{courseId}")
    public CourseOrder create(@PathVariable Long courseId, HttpSession session) {
        Long uid = getUid(session);
        return orderService.createOrder(courseId, uid);
    }

    // 支付
    @PostMapping("/{orderId}/pay")
    public CourseOrder pay(@PathVariable Long orderId, HttpSession session) {
        Long uid = getUid(session);
        return orderService.pay(orderId, uid);
    }

    // 取消（仅 UNPAID）
    @PostMapping("/{orderId}/cancel")
    public CourseOrder cancel(@PathVariable Long orderId, HttpSession session) {
        Long uid = getUid(session);
        return orderService.cancel(orderId, uid);
    }

    // ✅ 退款（仅 PAID -> REFUNDED）
    @PostMapping("/{orderId}/refund")
    public CourseOrder refund(@PathVariable Long orderId, HttpSession session) {
        Long uid = getUid(session);
        return orderService.refund(orderId, uid);
    }

    // 我的订单
    @GetMapping("/my")
    public List<CourseOrder> my(HttpSession session) {
        Long uid = getUid(session);
        return orderService.myOrders(uid);
    }

    private Long getUid(HttpSession session) {
        Object uid = session.getAttribute("LOGIN_USER_ID");
        return uid == null ? null : Long.valueOf(uid.toString());
    }
}
