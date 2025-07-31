package kr.co.govengers.config; // 패키지 이름은 kr.co.govengers.config로 통일합니다.

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.properties에 설정된 파일 업로드 경로를 가져옵니다.
    @Value("${custom.upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 웹 브라우저에서 /gogiImage/** 로 시작하는 모든 요청을
        // 서버의 실제 파일 경로(예: C:/gogiImage/)에 연결합니다.
        registry.addResourceHandler("/gogiImage/**")
                .addResourceLocations("file:///" + uploadPath + "/");
    }
}