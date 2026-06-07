package com.graduation.crafttrace.common;

import javax.servlet.http.HttpSession;

public class Authz {

    public static Long uid(HttpSession session) {
        if (session == null) throw new UnauthorizedException("未登录");
        Object v = session.getAttribute(AuthConst.UID);
        if (v == null) throw new UnauthorizedException("未登录");
        return Long.valueOf(String.valueOf(v));
    }

    public static String role(HttpSession session) {
        if (session == null) return "";
        Object v = session.getAttribute(AuthConst.ROLE);
        return v == null ? "" : String.valueOf(v).trim();
    }

    public static boolean isAdmin(HttpSession session) {
        return "admin".equalsIgnoreCase(role(session));
    }

    public static boolean isHandicraft(HttpSession session) {
        return "handicraft".equalsIgnoreCase(role(session));
    }

    public static boolean isStudent(HttpSession session) {
        return "student".equalsIgnoreCase(role(session));
    }

    public static void requireLogin(HttpSession session) {
        uid(session);
    }

    public static void requireAdmin(HttpSession session) {
        uid(session);
        if (!isAdmin(session)) {
            throw new ForbiddenException("无权限：仅管理员可操作");
        }
    }

    public static void requireAdminOrHandicraft(HttpSession session) {
        uid(session);
        if (!isAdmin(session) && !isHandicraft(session)) {
            throw new ForbiddenException("无权限：仅管理员/匠人可操作");
        }
    }

    public static void requireStudent(HttpSession session) {
        uid(session);
        if (!isStudent(session)) {
            throw new ForbiddenException("无权限：仅学员可操作");
        }
    }
}
