package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.User;
import com.graduation.crafttrace.dto.LoginRequest;
import com.graduation.crafttrace.dto.LoginResponse;
import com.graduation.crafttrace.dto.RegisterRequest;
import com.graduation.crafttrace.repository.UserRepository;
import com.graduation.crafttrace.common.Result; // ✅ 确保第一步的文件已创建
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    // ✅ 与拦截器保持严格一致
    private static final String SESSION_UID = "LOGIN_USER_ID";
    private static final String SESSION_ROLE = "LOGIN_ROLE";
    private static final String SESSION_NAME = "LOGIN_NAME";
    private static final String SESSION_USERNAME = "LOGIN_USERNAME";

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req, HttpSession session) {
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()
                || req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            return new LoginResponse(false, "用户名或密码不能为空");
        }

        Optional<User> opt = userRepository.findByUsername(req.getUsername().trim());
        if (opt.isEmpty()) {
            return new LoginResponse(false, "用户名不存在");
        }

        User user = opt.get();
        if (!req.getPassword().equals(user.getPassword())) {
            return new LoginResponse(false, "密码错误");
        }

        // 写入 Session
        session.setAttribute(SESSION_UID, user.getId());
        session.setAttribute(SESSION_ROLE, user.getRole());
        session.setAttribute(SESSION_NAME, user.getName());
        session.setAttribute(SESSION_USERNAME, user.getUsername());

        // 构造返回给前端的对象
        LoginResponse resp = new LoginResponse(true, "登录成功");
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRole(user.getRole());
        resp.setName(user.getName());
        resp.setAvatar(user.getAvatar()); // ✅ 确保 User 类有 getAvatar() 方法
        return resp;
    }

    /**
     * 更新用户头像
     * 对应前端请求路径：/api/auth/update-avatar
     */
    @PostMapping("/update-avatar")
    public Result updateAvatar(@RequestBody Map<String, String> body, HttpSession session) {
        String avatarUrl = body.get("avatar");

        // 1. 从 Session 中获取登录时存入的 UID
        Object uidObj = session.getAttribute(SESSION_UID);
        if (uidObj == null) {
            return Result.error("会话已过期，请重新登录");
        }

        // 2. 转换 ID 类型并查询数据库
        Long uid = (uidObj instanceof Long) ? (Long) uidObj : Long.valueOf(String.valueOf(uidObj));
        Optional<User> userOpt = userRepository.findById(uid);

        if (userOpt.isPresent()) {
            User currentUser = userOpt.get();
            currentUser.setAvatar(avatarUrl); // ✅ 更新头像地址
            userRepository.save(currentUser); // ✅ 持久化到数据库
            return Result.ok("头像更新成功");
        } else {
            return Result.error("用户不存在");
        }
    }

    // 当前登录用户信息
    @GetMapping("/me")
    public LoginResponse me(HttpSession session) {
        Object uid = session.getAttribute(SESSION_UID);
        if (uid == null) {
            return new LoginResponse(false, "请先登录");
        }

        LoginResponse resp = new LoginResponse(true, "已登录");
        resp.setId((uid instanceof Long) ? (Long) uid : Long.valueOf(String.valueOf(uid)));
        resp.setUsername(String.valueOf(session.getAttribute(SESSION_USERNAME)));
        resp.setRole(String.valueOf(session.getAttribute(SESSION_ROLE)));
        resp.setName(String.valueOf(session.getAttribute(SESSION_NAME)));
        return resp;
    }

    // 退出登录
    @PostMapping("/logout")
    public LoginResponse logout(HttpSession session) {
        session.invalidate();
        return new LoginResponse(true, "已退出登录");
    }

    // 注册
    @PostMapping("/register")
    public LoginResponse register(@RequestBody RegisterRequest req) {
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()
                || req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            return new LoginResponse(false, "用户名或密码不能为空");
        }

        String username = req.getUsername().trim();
        if (userRepository.findByUsername(username).isPresent()) {
            return new LoginResponse(false, "用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(req.getPassword().trim());

        String roleIn = (req.getRole() == null) ? "" : req.getRole().trim();
        String role = "admin".equalsIgnoreCase(roleIn) ? "admin" :
                ("craft".equalsIgnoreCase(roleIn) || "handicraft".equalsIgnoreCase(roleIn) || "匠人".equals(roleIn))
                        ? "handicraft" : "student";
        user.setRole(role);
        user.setName((req.getName() == null || req.getName().trim().isEmpty()) ? username : req.getName().trim());
        user.setEmail((req.getEmail() == null || req.getEmail().trim().isEmpty()) ? null : req.getEmail().trim());

        userRepository.save(user);
        return new LoginResponse(true, "注册成功");
    }
}