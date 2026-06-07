package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.dto.TraceStepUpdateReq;
import com.graduation.crafttrace.entity.TraceStep;
import com.graduation.crafttrace.service.TraceStepService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api/trace")
public class TraceController {

    private final TraceStepService traceStepService;

    public TraceController(TraceStepService traceStepService) {
        this.traceStepService = traceStepService;
    }

    @PostMapping
    public TraceStep addStep(@RequestBody TraceStep step, HttpSession session) {
        ensureHandicraft(session);

        if (step == null) {
            throw new ResponseStatusException(BAD_REQUEST, "请求体不能为空");
        }
        if (step.getCraftItemId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "craftItemId 不能为空");
        }
        if (step.getStepNo() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "stepNo 不能为空");
        }
        if (step.getStepName() == null || step.getStepName().trim().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "stepName 不能为空");
        }

        Long userId = getUserId(session);
        if (userId != null) {
            step.setOperatorUserId(userId);
        }
        step.setStepName(step.getStepName().trim());
        return traceStepService.addStep(step);
    }

    @GetMapping("/{craftItemId}")
    public List<TraceStep> list(@PathVariable Long craftItemId) {
        if (craftItemId == null) {
            throw new ResponseStatusException(BAD_REQUEST, "craftItemId 不能为空");
        }
        return traceStepService.listSteps(craftItemId);
    }

    @PostMapping("/{craftItemId}/rebuild-hash")
    public String rebuildHash(@PathVariable Long craftItemId, HttpSession session) {
        String role = getRole(session);
        if (!"handicraft".equals(role) && !"admin".equals(role)) {
            throw new ResponseStatusException(FORBIDDEN, "仅匠人或管理员可重建工序哈希链");
        }
        if (craftItemId == null) {
            throw new ResponseStatusException(BAD_REQUEST, "craftItemId 不能为空");
        }

        traceStepService.rebuildHashes(craftItemId);
        return "OK";
    }

    @PutMapping("/step/{id}/status")
    public TraceStep updateStepStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpSession session
    ) {
        ensureHandicraft(session);

        if (id == null) {
            throw new ResponseStatusException(BAD_REQUEST, "id 不能为空");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "status 不能为空");
        }

        Long userId = getUserId(session);
        return traceStepService.updateStatus(id, status, userId);
    }

    @PutMapping("/step/{id}")
    public TraceStep updateStepContent(
            @PathVariable Long id,
            @RequestBody TraceStepUpdateReq req,
            HttpSession session
    ) {
        ensureHandicraft(session);

        if (id == null) {
            throw new ResponseStatusException(BAD_REQUEST, "id 不能为空");
        }
        if (req == null) {
            throw new ResponseStatusException(BAD_REQUEST, "请求体不能为空");
        }

        Long userId = getUserId(session);
        return traceStepService.updateContent(id, req, userId);
    }

    private void ensureHandicraft(HttpSession session) {
        if (!"handicraft".equals(getRole(session))) {
            throw new ResponseStatusException(FORBIDDEN, "仅匠人端可维护工序信息");
        }
    }

    private String getRole(HttpSession session) {
        Object roleObj = session.getAttribute("LOGIN_ROLE");
        return roleObj == null ? null : roleObj.toString();
    }

    private Long getUserId(HttpSession session) {
        Object uid = session.getAttribute("LOGIN_USER_ID");
        return uid == null ? null : Long.valueOf(uid.toString());
    }
}
