package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.User;
import com.graduation.crafttrace.common.Authz;
import com.graduation.crafttrace.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    // ✅ 与 AuthInterceptor/AuthController 一致
    private static final String SESSION_UID = "LOGIN_USER_ID";

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ 分页 + 搜索（仅管理员）
    @GetMapping
    public Map<String, Object> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            HttpSession session
    ) {
        Authz.requireAdmin(session);

        int p = Math.max(page, 1) - 1;
        int s = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));

        Page<User> result;
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            result = userRepository.findAll(pageable);
        } else {
            result = userRepository
                    .findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrRoleContainingIgnoreCase(
                            kw, kw, kw, pageable
                    );
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("list", result.getContent().stream().map(this::safeUser).toArray());
        resp.put("total", result.getTotalElements());
        resp.put("page", page);
        resp.put("size", size);
        return resp;
    }

    // ✅ 新增用户（仅管理员）
    @PostMapping
    public Map<String, Object> create(@RequestBody User req, HttpSession session) {
        Authz.requireAdmin(session);

        String username = req.getUsername() == null ? "" : req.getUsername().trim();
        String password = req.getPassword() == null ? "" : req.getPassword().trim();

        if (username.isEmpty() || password.isEmpty()) return fail("用户名或密码不能为空");
        if (userRepository.existsByUsername(username)) return fail("用户名已存在");

        User u = new User();
        u.setUsername(username);
        u.setPassword(password); // 你目前明文
        u.setRole(req.getRole() == null || req.getRole().trim().isEmpty() ? "student" : req.getRole().trim());
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPhone(req.getPhone());
        u.setAddress(req.getAddress());

        User saved = userRepository.save(u);

        Map<String, Object> resp = ok("创建成功");
        resp.put("user", safeUser(saved));
        return resp;
    }

    // ✅ 编辑用户（仅管理员）
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody User req, HttpSession session) {
        Authz.requireAdmin(session);

        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        if (req.getName() != null) user.setName(req.getName());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getAddress() != null) user.setAddress(req.getAddress());
        if (req.getRole() != null && !req.getRole().trim().isEmpty()) user.setRole(req.getRole().trim());

        if (req.getPassword() != null && !req.getPassword().trim().isEmpty()) {
            user.setPassword(req.getPassword().trim());
        }

        User saved = userRepository.save(user);

        Map<String, Object> resp = ok("更新成功");
        resp.put("user", safeUser(saved));
        return resp;
    }

    // ✅ 删除用户（仅管理员，且不允许删自己）
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id, HttpSession session) {
        Authz.requireAdmin(session);

        Object uid = session.getAttribute(SESSION_UID);
        if (uid != null && String.valueOf(uid).equals(String.valueOf(id))) {
            return fail("不能删除当前登录用户");
        }

        if (!userRepository.existsById(id)) return fail("用户不存在");
        userRepository.deleteById(id);
        return ok("删除成功");
    }

    // ✅ 分配角色（仅管理员）
    @PostMapping("/assign-role/{userId}")
    public Map<String, Object> assignRoleToUser(@PathVariable Long userId, @RequestParam String roleName, HttpSession session) {
        Authz.requireAdmin(session);

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(roleName);
        User saved = userRepository.save(user);

        Map<String, Object> resp = ok("角色分配成功");
        resp.put("user", safeUser(saved));
        return resp;
    }

    // ✅✅ 学员分页 + 搜索（role 固定 student）
    // GET /api/users/students?page=1&size=10&keyword=
    @GetMapping("/students")
    public Map<String, Object> studentsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword
    ) {
        int p = Math.max(page, 1) - 1;
        int s = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));
        String kw = keyword == null ? "" : keyword.trim();

        Page<User> result;
        if (kw.isEmpty()) {
            result = userRepository.findByRole("student", pageable);
        } else {
            result = userRepository.findByRoleAndUsernameContainingIgnoreCaseOrRoleAndNameContainingIgnoreCase(
                    "student", kw, "student", kw, pageable
            );
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("list", result.getContent().stream().map(this::safeUser).toArray());
        resp.put("total", result.getTotalElements());
        resp.put("page", page);
        resp.put("size", size);
        return resp;
    }

    // ✅✅ 新增学员（role 强制 student）
    // POST /api/users/students
    @PostMapping("/students")
    public Map<String, Object> createStudent(@RequestBody User req) {
        String username = req.getUsername() == null ? "" : req.getUsername().trim();
        String password = req.getPassword() == null ? "" : req.getPassword().trim();

        if (username.isEmpty() || password.isEmpty()) {
            return fail("用户名或密码不能为空");
        }
        if (userRepository.existsByUsername(username)) {
            return fail("用户名已存在");
        }

        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setRole("student"); // ✅ 强制学员
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPhone(req.getPhone());
        u.setAddress(req.getAddress());

        User saved = userRepository.save(u);

        Map<String, Object> resp = ok("学员创建成功");
        resp.put("user", safeUser(saved));
        return resp;
    }


    // -------- helpers ----------
    private Map<String, Object> ok(String msg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", true);
        m.put("message", msg);
        return m;
    }

    private Map<String, Object> fail(String msg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", false);
        m.put("message", msg);
        return m;
    }

    private Map<String, Object> safeUser(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("role", u.getRole());
        m.put("name", u.getName());
        m.put("email", u.getEmail());
        m.put("phone", u.getPhone());
        m.put("address", u.getAddress());
        return m;
    }
}
