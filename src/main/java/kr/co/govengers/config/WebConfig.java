package kr.co.govengers.config;

import jakarta.annotation.PostConstruct;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✅ 기본값은 C:/gogiImage (팀 합의 경로)
    @Value("${custom.upload-path:C:/gogiImage}")
    private String uploadPath;

    @PostConstruct
    public void createDirIfNotExists() {
        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 윈도우 'file:' 프리픽스는 마지막 슬래시 필수
        String normalized = uploadPath.endsWith("/") || uploadPath.endsWith("\\")
                ? uploadPath
                : uploadPath + File.separator;

        // /api/images/**, /images/**, /gogiImage/** 모두 같은 물리 경로로 매핑
        registry.addResourceHandler("/api/images/**", "/images/**", "/gogiImage/**")
                .addResourceLocations("file:" + normalized);
    }

    // (선택) 업로드 대용량 설정 유지
    @Bean
    public TomcatServletWebServerFactory containerFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected void customizeConnector(Connector connector) {
                super.customizeConnector(connector);
                if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol<?> http) {
                    http.setDisableUploadTimeout(false);
                    http.setConnectionUploadTimeout(240000);
                    http.setMaxSwallowSize(-1);
                }
            }
        };
    }
}
