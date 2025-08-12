package kr.co.govengers.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.root:uploads}")
    private String uploadRoot;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /api/images/** -> 파일시스템 uploads/
        registry.addResourceHandler("/api/images/**")
                .addResourceLocations("file:" + (uploadRoot.endsWith("/") ? uploadRoot : uploadRoot + "/"));
    }
}
