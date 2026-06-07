package com.graduation.crafttrace.dto;

import lombok.Data;

@Data
public class WorkshopVO {
    private Long id;            // 真实主键
    private String name;
    private String phone;
    private String address;

    private Long ownerUserId;   // 归属匠人ID
    private String ownerName;   // 归属匠人账号（用于展示）
    private String realName;    // ✅ 新增：归属匠人真实姓名（如 王德全师傅）
}