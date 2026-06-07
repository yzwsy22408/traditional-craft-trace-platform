package com.graduation.crafttrace.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "material")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 名称
    @Column(nullable = false, length = 200)
    private String name;

    // 分类
    @Column(length = 100)
    private String category;

    // 单位（kg/米/包）
    @Column(length = 50)
    private String unit;

    // 库存
    private Integer stock;

    // 单价
    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    // 供应商
    @Column(length = 200)
    private String supplier;

    // 备注
    @Column(length = 2000)
    private String description;

    // 图片URL
    @Column(length = 500)
    private String imageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;

        if (stock == null) stock = 0;
        if (price == null) price = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        if (stock == null) stock = 0;
        if (price == null) price = BigDecimal.ZERO;
    }
}
