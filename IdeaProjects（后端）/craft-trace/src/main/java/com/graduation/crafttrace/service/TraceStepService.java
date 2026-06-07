package com.graduation.crafttrace.service;

import com.graduation.crafttrace.common.HashUtil;
import com.graduation.crafttrace.dto.TraceStepUpdateReq;
import com.graduation.crafttrace.entity.TraceStep;
import com.graduation.crafttrace.repository.TraceStepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class TraceStepService {

    private final TraceStepRepository traceStepRepository;
    private final MaterialService materialService;

    public TraceStepService(TraceStepRepository traceStepRepository, MaterialService materialService) {
        this.traceStepRepository = traceStepRepository;
        this.materialService = materialService;
    }

    @Transactional
    public TraceStep addStep(TraceStep step) {
        if (step == null || step.getCraftItemId() == null || step.getStepNo() == null || isBlank(step.getStepName())) {
            throw new ResponseStatusException(BAD_REQUEST, "工序信息不完整");
        }
        if (traceStepRepository.existsByCraftItemIdAndStepNo(step.getCraftItemId(), step.getStepNo())) {
            throw new ResponseStatusException(BAD_REQUEST, "该工艺品下已存在相同步骤编号");
        }

        if (!isBlank(step.getMaterials())) {
            materialService.deductStockFromJson(step.getMaterials());
        }

        if (step.getOperateTime() == null) {
            step.setOperateTime(LocalDateTime.now());
        }
        if (isBlank(step.getStatus())) {
            step.setStatus("NOT_STARTED");
        }
        step.setStepName(step.getStepName().trim());

        TraceStep saved = traceStepRepository.save(step);
        rebuildHashes(saved.getCraftItemId());
        return traceStepRepository.findById(saved.getId()).orElse(saved);
    }

    @Transactional(readOnly = true)
    public List<TraceStep> listSteps(Long craftItemId) {
        return traceStepRepository.findByCraftItemIdOrderByStepNoAsc(craftItemId);
    }

    @Transactional
    public void rebuildHashes(Long craftItemId) {
        List<TraceStep> steps = traceStepRepository.findByCraftItemIdOrderByStepNoAsc(craftItemId);
        String prev = "GENESIS";
        for (TraceStep step : steps) {
            String payload = buildPayload(step);
            String payloadHash = HashUtil.sha256(payload);
            String stepHash = HashUtil.chainHash(prev, payloadHash);
            step.setPrevHash(prev);
            step.setPayloadHash(payloadHash);
            step.setStepHash(stepHash);
            prev = stepHash;
        }
        traceStepRepository.saveAll(steps);
    }

    @Transactional(readOnly = true)
    public VerifyResult verifyHashes(Long craftItemId) {
        if (craftItemId == null) {
            throw new ResponseStatusException(BAD_REQUEST, "工艺品 ID 不能为空");
        }

        List<TraceStep> steps = traceStepRepository.findByCraftItemIdOrderByStepNoAsc(craftItemId);
        String prev = "GENESIS";
        for (TraceStep step : steps) {
            String payload = buildPayload(step);
            String expectPayloadHash = HashUtil.sha256(payload);
            String expectStepHash = HashUtil.chainHash(prev, expectPayloadHash);

            if (!eq(step.getPrevHash(), prev)) {
                return VerifyResult.bad(step.getStepNo(), "前序哈希不匹配");
            }
            if (!eq(step.getPayloadHash(), expectPayloadHash)) {
                return VerifyResult.bad(step.getStepNo(), "步骤内容哈希不匹配");
            }
            if (!eq(step.getStepHash(), expectStepHash)) {
                return VerifyResult.bad(step.getStepNo(), "链式哈希不匹配");
            }
            prev = expectStepHash;
        }
        return VerifyResult.ok();
    }

    @Transactional
    public TraceStep updateStatus(Long id, String status, Long userId) {
        TraceStep step = traceStepRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "未找到对应工序"));
        step.setStatus(status.trim().toUpperCase());
        step.setOperateTime(LocalDateTime.now());
        if (userId != null) {
            step.setOperatorUserId(userId);
        }
        TraceStep saved = traceStepRepository.save(step);
        rebuildHashes(saved.getCraftItemId());
        return saved;
    }

    @Transactional
    public TraceStep updateContent(Long id, TraceStepUpdateReq req, Long userId) {
        TraceStep step = traceStepRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "未找到对应工序"));

        if (req.getDetail() != null) {
            step.setDetail(req.getDetail());
        }
        if (req.getMaterials() != null) {
            step.setMaterials(req.getMaterials());
        }
        if (req.getImages() != null) {
            step.setImages(req.getImages());
        }
        if (req.getVideoUrl() != null) {
            step.setVideoUrl(req.getVideoUrl());
        }

        if (userId != null) {
            step.setOperatorUserId(userId);
        }
        step.setOperateTime(LocalDateTime.now());

        TraceStep saved = traceStepRepository.save(step);
        rebuildHashes(saved.getCraftItemId());
        return saved;
    }

    private String buildPayload(TraceStep step) {
        return HashUtil.payloadOf(
                step.getCraftItemId(),
                step.getStepNo(),
                step.getStepName(),
                step.getDetail(),
                step.getStatus(),
                step.getImages(),
                step.getVideoUrl(),
                step.getMaterials()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean eq(String left, String right) {
        return (left == null ? "" : left.trim()).equals(right == null ? "" : right.trim());
    }

    public static class VerifyResult {
        private boolean ok;
        private Integer brokenAtStepNo;
        private String message;

        public static VerifyResult ok() {
            VerifyResult result = new VerifyResult();
            result.ok = true;
            result.message = "OK";
            return result;
        }

        public static VerifyResult bad(Integer stepNo, String msg) {
            VerifyResult result = new VerifyResult();
            result.ok = false;
            result.brokenAtStepNo = stepNo;
            result.message = msg;
            return result;
        }

        public boolean isOk() {
            return ok;
        }

        public void setOk(boolean ok) {
            this.ok = ok;
        }

        public Integer getBrokenAtStepNo() {
            return brokenAtStepNo;
        }

        public void setBrokenAtStepNo(Integer brokenAtStepNo) {
            this.brokenAtStepNo = brokenAtStepNo;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
