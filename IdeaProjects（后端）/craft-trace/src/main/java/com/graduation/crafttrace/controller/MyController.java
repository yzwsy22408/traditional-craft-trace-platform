package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.entity.Course;
import com.graduation.crafttrace.entity.CourseBooking;
import com.graduation.crafttrace.entity.CourseOrder;
import com.graduation.crafttrace.repository.CourseBookingRepository;
import com.graduation.crafttrace.repository.CourseOrderRepository;
import com.graduation.crafttrace.repository.CourseRepository;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/my")
public class MyController {

    private final CourseRepository courseRepository;
    private final CourseBookingRepository bookingRepository;
    private final CourseOrderRepository orderRepository;

    public MyController(CourseRepository courseRepository,
                        CourseBookingRepository bookingRepository,
                        CourseOrderRepository orderRepository) {
        this.courseRepository = courseRepository;
        this.bookingRepository = bookingRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * 我的课程（已报名 + 已下单）
     * ✅ 按“最近一次操作时间”排序：报名时间/下单时间/支付时间（谁最新谁排前）
     */
    @GetMapping("/courses")
    public Map<String, Object> myCourses(HttpSession session) {
        Long uid = getUid(session);
        if (uid == null) throw new RuntimeException("未登录");

        List<CourseBooking> bookings =
                bookingRepository.findByStudentUserIdOrderByBookingTimeDesc(uid);
        List<CourseOrder> orders =
                orderRepository.findByStudentUserIdOrderByCreatedAtDesc(uid);

        // 1) 计算每个 courseId 的“最近操作时间”
        Map<Long, LocalDateTime> lastTimeMap = new HashMap<>();

        for (CourseBooking b : bookings) {
            Long cid = b.getCourseId();
            LocalDateTime t = b.getBookingTime();
            mergeLastTime(lastTimeMap, cid, t);
        }

        for (CourseOrder o : orders) {
            Long cid = o.getCourseId();

            // 订单的“操作时间”取最大：支付时间 > 创建时间
            LocalDateTime t = o.getCreatedAt();
            if (o.getPayTime() != null) t = o.getPayTime();

            mergeLastTime(lastTimeMap, cid, t);
        }

        // 2) 把 courseId 按 lastTime 倒序排序
        List<Long> sortedCourseIds = lastTimeMap.entrySet().stream()
                .sorted((a, b) -> {
                    LocalDateTime ta = a.getValue();
                    LocalDateTime tb = b.getValue();
                    // null 放后面
                    if (ta == null && tb == null) return 0;
                    if (ta == null) return 1;
                    if (tb == null) return -1;
                    return tb.compareTo(ta);
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 3) 批量查课程并按排序结果保序
        List<Course> courses = sortedCourseIds.isEmpty()
                ? Collections.emptyList()
                : courseRepository.findAllById(sortedCourseIds);

        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        List<Course> orderedCourses = new ArrayList<>();
        for (Long cid : sortedCourseIds) {
            Course c = courseMap.get(cid);
            if (c != null) orderedCourses.add(c);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("courses", orderedCourses);
        resp.put("bookings", bookings);
        resp.put("orders", orders);
        return resp;
    }

    private void mergeLastTime(Map<Long, LocalDateTime> map, Long courseId, LocalDateTime t) {
        if (courseId == null) return;
        LocalDateTime old = map.get(courseId);
        if (old == null) {
            map.put(courseId, t);
            return;
        }
        if (t != null && t.isAfter(old)) {
            map.put(courseId, t);
        }
    }

    private Long getUid(HttpSession session) {
        Object uid = session.getAttribute("LOGIN_USER_ID");
        return uid == null ? null : Long.valueOf(uid.toString());
    }
}
