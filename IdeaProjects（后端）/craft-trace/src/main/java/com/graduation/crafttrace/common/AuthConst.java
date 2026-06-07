package com.graduation.crafttrace.common;

/**
 * ✅ 统一 Session Key
 * 你项目里 Controller 一直使用：LOGIN_USER_ID / LOGIN_ROLE
 * 所以 Authz / Interceptor 也必须统一用这个
 */
public class AuthConst {
    public static final String UID  = "LOGIN_USER_ID";
    public static final String ROLE = "LOGIN_ROLE";
}
