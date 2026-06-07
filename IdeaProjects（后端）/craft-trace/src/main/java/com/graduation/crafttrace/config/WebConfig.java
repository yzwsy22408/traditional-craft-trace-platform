package com.graduation.crafttrace.config;

import com.graduation.crafttrace.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    /**
     * ✅ 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * ✅ 静态资源映射优化
     * 确保浏览器访问 /uploads/** 时，能精准找到硬盘文件夹
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录的绝对路径
        String rootPath = System.getProperty("user.dir").replace("\\", "/");
        // 确保路径以斜杠结尾
        if (!rootPath.endsWith("/")) {
            rootPath += "/";
        }
        String uploadPath = rootPath + "uploads/";

        // 自动创建文件夹，防止因为文件夹不存在导致映射失败
        File folder = new File(uploadPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // 映射配置：将 URL 路径 /uploads/** 映射到本地文件路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

    /**
     * ✅ 拦截器配置
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/me",
                        "/api/auth/logout",
                        "/api/auth/register",
                        "/api/public/**",
                        "/api/upload", // ✅ 已放行上传接口
                        "/error",
                        "/**/*.html",
                        "/**/*.js",
                        "/**/*.css",
                        "/**/*.png",
                        "/**/*.jpg",
                        "/**/*.jpeg",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/favicon.ico",
                        "/**/*.map"
                );
    }
}