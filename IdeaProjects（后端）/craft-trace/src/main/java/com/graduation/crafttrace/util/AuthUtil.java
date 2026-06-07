package com.graduation.crafttrace.util;

import com.graduation.crafttrace.common.AuthConst;

import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AuthUtil {

    public static Long getCurrentUserId() {
        ServletRequestAttributes attr =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attr == null) return null;

        HttpSession session = attr.getRequest().getSession(false);
        if (session == null) return null;

        Object uid = session.getAttribute(AuthConst.UID);
        if (uid == null) return null;

        return Long.valueOf(String.valueOf(uid));
    }
}
