package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.User;
import com.graduation.crafttrace.common.AuthConst;
import com.graduation.crafttrace.common.ForbiddenException;
import com.graduation.crafttrace.common.UnauthorizedException;
import com.graduation.crafttrace.dto.WorkshopVO;
import com.graduation.crafttrace.entity.Workshop;
import com.graduation.crafttrace.repository.UserRepository;
import com.graduation.crafttrace.service.WorkshopService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workshop")
public class WorkshopController {

    private final WorkshopService workshopService;
    private final UserRepository userRepository;

    public WorkshopController(WorkshopService workshopService, UserRepository userRepository) {
        this.workshopService = workshopService;
        this.userRepository = userRepository;
    }

    /**
     * 工坊列表查询：管理员/学生看全部（公开），匠人看自己
     */
    @GetMapping
    public List<WorkshopVO> list(HttpSession session) {
        getUid(session);
        String role = getRole(session);

        List<Workshop> list;
        if ("admin".equals(role) || "student".equals(role)) {
            list = workshopService.listAll();
        } else {
            list = workshopService.listByOwner(getUid(session));
        }

        // ✅ 核心修复：获取完整的用户信息映射
        Set<Long> ownerIds = list.stream().map(Workshop::getOwnerUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return list.stream().map(w -> {
            WorkshopVO vo = new WorkshopVO();
            vo.setId(w.getId()); vo.setName(w.getName()); vo.setPhone(w.getPhone());
            vo.setAddress(w.getAddress()); vo.setOwnerUserId(w.getOwnerUserId());

            User u = userMap.get(w.getOwnerUserId());
            if (u != null) {
                String displayName = (u.getName() != null && !u.getName().trim().isEmpty())
                        ? u.getName().trim()
                        : u.getUsername();
                vo.setOwnerName(displayName);
                vo.setRealName(displayName);
            } else {
                vo.setOwnerName("unbound");
                vo.setRealName("未绑定");
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 更换匠人接口
     */
    @PostMapping("/{id}/assign-owner")
    public Workshop assignOwner(@PathVariable Long id, @RequestParam Long ownerUserId, HttpSession session) {
        if (!"admin".equals(getRole(session))) {
            throw new ForbiddenException("只有管理员可以更换工坊负责人");
        }
        return workshopService.assignOwner(id, ownerUserId);
    }

    @PostMapping
    public Workshop create(@RequestBody Workshop req, HttpSession session) {
        String role = getRole(session);
        if ("student".equals(role)) throw new ForbiddenException("学生无权创建工坊");
        return workshopService.create(req, getUid(session));
    }

    @PutMapping("/{id}")
    public Workshop update(@PathVariable Long id, @RequestBody Workshop req, HttpSession session) {
        getUid(session);
        return workshopService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id, HttpSession session) {
        getUid(session);
        workshopService.delete(id);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        return resp;
    }

    private Long getUid(HttpSession session) {
        Object v = session.getAttribute(AuthConst.UID);
        if (v == null) throw new UnauthorizedException("未登录");
        return Long.valueOf(String.valueOf(v));
    }

    private String getRole(HttpSession session) {
        Object v = session.getAttribute(AuthConst.ROLE);
        return v == null ? "" : String.valueOf(v);
    }
}
