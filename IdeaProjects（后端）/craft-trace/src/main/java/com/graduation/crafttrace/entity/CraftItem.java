package com.graduation.crafttrace.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "craft_item")
public class CraftItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String category;

    @Column(length = 2000)
    private String description;

    private String imageUrl;

    private Long createdBy;

    private Long artisanId;

    @Column(length = 100)
    private String artisanName;

    @Column(length = 150)
    private String artisanTitle;

    @Column(length = 150)
    private String workshopName;

    @Column(length = 200)
    private String displayLocation;

    @Column(length = 200)
    private String qrPlacement;

    @Column(columnDefinition = "TEXT")
    private String materialSourceSummary;

    @Column(columnDefinition = "TEXT")
    private String materialSources;

    @Column(columnDefinition = "TEXT")
    private String traceNotice;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        trimFields();
    }

    @PreUpdate
    public void preUpdate() {
        trimFields();
    }

    private void trimFields() {
        code = normalize(code);
        name = normalize(name);
        category = normalize(category);
        description = normalize(description);
        imageUrl = normalize(imageUrl);
        artisanName = normalize(artisanName);
        artisanTitle = normalize(artisanTitle);
        workshopName = normalize(workshopName);
        displayLocation = normalize(displayLocation);
        qrPlacement = normalize(qrPlacement);
        materialSourceSummary = normalize(materialSourceSummary);
        materialSources = normalize(materialSources);
        traceNotice = normalize(traceNotice);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Long getArtisanId() { return artisanId; }
    public void setArtisanId(Long artisanId) { this.artisanId = artisanId; }
    public String getArtisanName() { return artisanName; }
    public void setArtisanName(String artisanName) { this.artisanName = artisanName; }
    public String getArtisanTitle() { return artisanTitle; }
    public void setArtisanTitle(String artisanTitle) { this.artisanTitle = artisanTitle; }
    public String getWorkshopName() { return workshopName; }
    public void setWorkshopName(String workshopName) { this.workshopName = workshopName; }
    public String getDisplayLocation() { return displayLocation; }
    public void setDisplayLocation(String displayLocation) { this.displayLocation = displayLocation; }
    public String getQrPlacement() { return qrPlacement; }
    public void setQrPlacement(String qrPlacement) { this.qrPlacement = qrPlacement; }
    public String getMaterialSourceSummary() { return materialSourceSummary; }
    public void setMaterialSourceSummary(String materialSourceSummary) { this.materialSourceSummary = materialSourceSummary; }
    public String getMaterialSources() { return materialSources; }
    public void setMaterialSources(String materialSources) { this.materialSources = materialSources; }
    public String getTraceNotice() { return traceNotice; }
    public void setTraceNotice(String traceNotice) { this.traceNotice = traceNotice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
