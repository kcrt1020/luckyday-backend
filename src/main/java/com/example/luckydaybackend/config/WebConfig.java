package com.example.luckydaybackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:/app/uploads}") // ✅ 환경 변수 사용 가능
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/") // ✅ 컨테이너 내부의 `/app/uploads/` 경로
                .setCachePeriod(3600) // ✅ 캐시 설정
                .resourceChain(true);
    }
}