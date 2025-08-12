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

    @Value("${custom.upload-path}")
    private String uploadPath;

    @PostConstruct
    public void createDirIfNotExists() {
        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String normalizedPath = normalizeUploadPath(uploadPath);

        registry.addResourceHandler("/gogiImage/**")
                .addResourceLocations("file:" + normalizedPath);

        registry.addResourceHandler("/api/images/**")
                .addResourceLocations("file:" + normalizedPath);

        registry.addResourceHandler("/api/imgs/**")
                .addResourceLocations("file:" + normalizedPath);

        registry.addResourceHandler("/api/download/**")
                .addResourceLocations("file:" + normalizedPath);
    }

    private String normalizeUploadPath(String path) {
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        return path;
    }

    @Bean
    public TomcatServletWebServerFactory containerFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected void customizeConnector(Connector connector) {
                super.customizeConnector(connector);
                if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol) {
                    AbstractHttp11Protocol<?> httpProtocol = (AbstractHttp11Protocol<?>) connector.getProtocolHandler();
                    httpProtocol.setDisableUploadTimeout(false);
                    httpProtocol.setConnectionUploadTimeout(240000);
                    httpProtocol.setMaxSwallowSize(-1);
                }
            }
        };
    }

}