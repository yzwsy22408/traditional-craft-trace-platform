package com.graduation.crafttrace.dto;

public class CraftUpdateReq {

    private String code;
    private String name;
    private String category;
    private String description;
    private String imageUrl;
    private Long artisanId;
    private String artisanName;
    private String artisanTitle;
    private String workshopName;
    private String displayLocation;
    private String qrPlacement;
    private String materialSourceSummary;
    private String materialSources;
    private String traceNotice;

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
}
