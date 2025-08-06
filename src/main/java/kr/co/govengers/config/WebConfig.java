package kr.co.govengers.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.MultipartConfigElement;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${custom.upload-path}")
    private String uploadPath;

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
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        factory.setMaxRequestSize(DataSize.ofMegabytes(10));
        return factory.createMultipartConfig();
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

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}