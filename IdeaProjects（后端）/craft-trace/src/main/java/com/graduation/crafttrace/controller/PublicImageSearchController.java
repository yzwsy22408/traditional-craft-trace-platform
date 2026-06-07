package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.common.Result;
import com.graduation.crafttrace.service.ImageSimilarityService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/public")
public class PublicImageSearchController {

    private final ImageSimilarityService imageSimilarityService;

    public PublicImageSearchController(ImageSimilarityService imageSimilarityService) {
        this.imageSimilarityService = imageSimilarityService;
    }

    @PostMapping("/image-search")
    public Result imageSearch(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        Result result = Result.ok("图像相似性检索完成");
        result.setData(imageSimilarityService.search(file, limit));
        return result;
    }
}
