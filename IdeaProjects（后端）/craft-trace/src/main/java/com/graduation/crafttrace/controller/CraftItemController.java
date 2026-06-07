package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.dto.CraftUpdateReq;
import com.graduation.crafttrace.entity.CraftItem;
import com.graduation.crafttrace.service.CraftItemService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/craft")
public class CraftItemController {

    private final CraftItemService craftItemService;

    public CraftItemController(CraftItemService craftItemService) {
        this.craftItemService = craftItemService;
    }

    private Long requireLogin(HttpSession session) {
        Object uid = session.getAttribute("LOGIN_USER_ID");
        if (uid == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "请先登录");
        }
        return Long.valueOf(uid.toString());
    }

    private String currentRole(HttpSession session) {
        Object role = session.getAttribute("LOGIN_ROLE");
        return role == null ? "" : String.valueOf(role);
    }

    @PostMapping
    public CraftItem create(@RequestBody CraftItem item, HttpSession session) {
        Long uid = requireLogin(session);
        return craftItemService.create(item, uid, currentRole(session));
    }

    @GetMapping
    public List<CraftItem> list() {
        return craftItemService.findAll();
    }

    @GetMapping("/{id}")
    public CraftItem get(@PathVariable Long id) {
        return craftItemService.findById(id);
    }

    @PutMapping("/{id}")
    public CraftItem update(@PathVariable Long id, @RequestBody CraftUpdateReq req, HttpSession session) {
        Long uid = requireLogin(session);
        return craftItemService.update(id, req, uid, currentRole(session));
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id, HttpSession session) {
        Long uid = requireLogin(session);
        craftItemService.delete(id, uid, currentRole(session));

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "删除成功");
        return resp;
    }
}
