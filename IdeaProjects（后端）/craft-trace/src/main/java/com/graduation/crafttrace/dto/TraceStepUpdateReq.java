package com.graduation.crafttrace.dto;

public class TraceStepUpdateReq {
    private String detail;
    private String materials; // JSON 字符串，比如 ["丝线","底布"]
    private String images;    // JSON 字符串，比如 ["url1","url2"]
    private String videoUrl;

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getMaterials() { return materials; }
    public void setMaterials(String materials) { this.materials = materials; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
}
