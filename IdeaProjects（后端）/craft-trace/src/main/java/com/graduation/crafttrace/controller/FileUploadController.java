package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
public class FileUploadController {

    /**
     * 文件上传接口
     * 前端 el-upload 的 action 要对应这个路径：/api/upload
     */
    @PostMapping("/api/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件为空");
        }

        // 1. 定义存储路径（存放在项目根目录下的 uploads 文件夹内）
        String folderPath = System.getProperty("user.dir") + "/uploads/";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs(); // 如果文件夹不存在就创建一个
        }

        // 2. 生成唯一文件名，防止重名覆盖
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;

        try {
            // 3. 保存文件到硬盘
            file.transferTo(new File(folderPath + fileName));

            // 4. 返回图片的访问地址给前端
            // 注意：这里暂时返回相对路径，前端需要配置静态资源映射才能看到图
            String url = "/uploads/" + fileName;

            Result result = Result.ok("上传成功");
            result.setData(url); // 把地址塞进 Result 的 data 里
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件保存失败：" + e.getMessage());
        }
    }
}