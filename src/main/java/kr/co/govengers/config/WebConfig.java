package kr.co.govengers.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${custom.upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/gogiImage/**")
                .addResourceLocations("file:///" + uploadPath + "/");

        registry.addResourceHandler("/api/imgs/**")
                .addResourceLocations("file:///" + uploadPath + "/");

        registry.addResourceHandler("/api/download/**")
                .addResourceLocations("file:///" + uploadPath + "/");
    }

    //SecurityConfig와 중복된 내용
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/api/**")
//                .allowedOrigins(
//                        "http://localhost",
//                        "http://127.0.0.1"
//                )
//                .allowCredentials(true)
//                .allowedMethods("*");
//    }
}
