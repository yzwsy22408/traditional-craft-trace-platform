package com.graduation.crafttrace.interceptor;

import com.graduation.crafttrace.common.AuthConst;
import com.graduation.crafttrace.common.Authz;
import com.graduation.crafttrace.common.ForbiddenException;
import com.graduation.crafttrace.common.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // 1) 放行 OPTIONS 预检
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        final String uri = request.getRequestURI();
        final String method = request.getMethod();

        // 2) 放行公开接口
        if (isPublic(uri)) return true;

        // 3) 只拦 /api/**
        if (!uri.startsWith("/api/")) return true;

        // 4) 登录校验
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(AuthConst.UID) == null) {
            throw new UnauthorizedException("请先登录");
        }

        String role = Authz.role(session);
        if (role == null || role.trim().isEmpty()) {
            throw new ForbiddenException("无权限访问");
        }

        // ===================== 5) RBAC 权限修复开始 =====================

        // 5.1 用户管理：仅管理员
        if (uri.startsWith("/api/users")) {
            Authz.requireAdmin(session);
            return true;
        }

        // 5.2 工坊：✅ 修复：允许学生通过 GET 请求查看工坊信息，解决首页报错
        if (uri.startsWith("/api/workshop")) {
            if ("GET".equalsIgnoreCase(method)) {
                Authz.requireLogin(session); // 只要登录了（包括学生）就能看
            } else {
                // 新增、编辑、删除、分配负责人等操作依然要求管理员/匠人
                if (uri.startsWith("/api/workshop/unbound") || uri.contains("/assign-owner")) {
                    Authz.requireAdmin(session);
                } else {
                    Authz.requireAdminOrHandicraft(session);
                }
            }
            return true;
        }

        // ✅✅ 5.2.1 材料管理
        if (uri.startsWith("/api/material")) {
            Authz.requireAdminOrHandicraft(session);
            return true;
        }

        // 5.3 课程管理：✅ 修复：允许学生访问公开课程接口和基础列表查询
        if (uri.startsWith("/api/course")) {
            if (uri.contains("/public/") || ("GET".equalsIgnoreCase(method) && uri.equals("/api/course"))) {
                Authz.requireLogin(session); // 允许学生 GET 查看
            } else {
                Authz.requireAdminOrHandicraft(session);
            }
            return true;
        }

        // 5.4 报名 booking
        if (uri.startsWith("/api/booking/course")) {
            Authz.requireAdminOrHandicraft(session);
            return true;
        }
        if (uri.startsWith("/api/booking")) {
            Authz.requireStudent(session);
            return true;
        }

        // 5.5 签到 attendance
        if (uri.startsWith("/api/attendance/course")) {
            Authz.requireAdminOrHandicraft(session);
            return true;
        }
        if (uri.startsWith("/api/attendance/sign") || uri.startsWith("/api/attendance/my")) {
            Authz.requireStudent(session);
            return true;
        }

        // 5.6 评价 review
        if (uri.startsWith("/api/review/course")) {
            boolean isMy = uri.endsWith("/my");
            boolean isGet = "GET".equalsIgnoreCase(method);
            if (isGet && !isMy) {
                Authz.requireLogin(session);
            } else {
                Authz.requireStudent(session);
            }
            return true;
        }
        if (uri.startsWith("/api/review/my")) {
            Authz.requireStudent(session);
            return true;
        }

        // 5.7 我的中心 & 5.8 订单
        if (uri.startsWith("/api/my") || uri.startsWith("/api/order")) {
            Authz.requireStudent(session);
            return true;
        }

        // 5.9 工艺品/溯源：管理员/匠人
        if (uri.startsWith("/api/craft") || uri.startsWith("/api/trace")) {
            Authz.requireAdminOrHandicraft(session);
            return true;
        }

        Authz.requireLogin(session);
        return true;
    }

    private boolean isPublic(String uri) {
        if (uri.startsWith("/api/auth/")) return true;
        if (uri.startsWith("/api/public/")) return true;
        return uri.startsWith("/error") || uri.startsWith("/favicon.ico");
    }
}