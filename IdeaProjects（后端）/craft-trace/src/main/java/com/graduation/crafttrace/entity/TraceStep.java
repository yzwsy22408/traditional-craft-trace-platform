package com.graduation.crafttrace.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trace_step")
public class TraceStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联工艺品 craft_item.id
    @Column(nullable = false)
    private Long craftItemId;

    // 第几步
    @Column(nullable = false)
    private Integer stepNo;

    // 工序名称
    @Column(nullable = false)
    private String stepName;

    // 工序描述
    @Column(length = 3000)
    private String detail;

    // 工序状态：NOT_STARTED / IN_PROGRESS / COMPLETED
    @Column(nullable = false)
    private String status;

    // 工序图片（JSON数组字符串）
    @Lob
    @Column(columnDefinition = "TEXT")
    private String images;

    // 工序视频地址（对应 video_url）
    @Column(name = "video_url", length = 255)
    private String videoUrl;

    // 使用材料（JSON数组字符串）
    @Lob
    @Column(columnDefinition = "TEXT")
    private String materials;

    // ======== 溯源哈希增强 ========

    // 前一节点 hash（链式）
    @Column(name = "prev_hash", length = 64)
    private String prevHash;

    // 当前步骤内容 hash（payload）
    @Column(name = "payload_hash", length = 64)
    private String payloadHash;

    // 最终链 hash（prevHash + payloadHash）
    @Column(name = "step_hash", length = 64)
    private String stepHash;

    // ====================================

    // 操作人（存 user.id）
    private Long operatorUserId;

    // 操作时间
    private LocalDateTime operateTime;

    @PrePersist
    public void prePersist() {
        if (operateTime == null) operateTime = LocalDateTime.now();
        if (status == null || status.trim().isEmpty()) status = "NOT_STARTED";
        if (stepName != null) stepName = stepName.trim();
    }

    // ================= getter / setter =================

    public Long getId() { return id; }

    public Long getCraftItemId() { return craftItemId; }
    public void setCraftItemId(Long craftItemId) { this.craftItemId = craftItemId; }

    public Integer getStepNo() { return stepNo; }
    public void setStepNo(Integer stepNo) { this.stepNo = stepNo; }

    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getMaterials() { return materials; }
    public void setMaterials(String materials) { this.materials = materials; }

    public String getPrevHash() { return prevHash; }
    public void setPrevHash(String prevHash) { this.prevHash = prevHash; }

    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }

    public String getStepHash() { return stepHash; }
    public void setStepHash(String stepHash) { this.stepHash = stepHash; }

    public Long getOperatorUserId() { return operatorUserId; }
    public void setOperatorUserId(Long operatorUserId) { this.operatorUserId = operatorUserId; }

    public LocalDateTime getOperateTime() { return operateTime; }
    public void setOperateTime(LocalDateTime operateTime) { this.operateTime = operateTime; }
}
