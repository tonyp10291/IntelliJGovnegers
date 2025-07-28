package kr.co.govengers.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String os = System.getProperty("os.name").toLowerCase();
        String filePath;

        if (os.contains("win")) {
            filePath = "file:///C:/your/windows/path/";
        } else {
            filePath = "file:/Users/minhopark/Documents/Java/govengers/gogiImage/";
        }

        registry.addResourceHandler("/api/imgs/**")
                .addResourceLocations(filePath);
        registry.addResourceHandler("/api/download/**")
                .addResourceLocations(filePath);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://127.0.0.1:3000"
                )
                .allowCredentials(true)
                .allowedMethods("*");
    }
}
