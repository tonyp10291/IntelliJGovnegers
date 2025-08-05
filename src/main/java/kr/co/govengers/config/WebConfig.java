package kr.co.govengers.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.MultipartConfigElement;

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


    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.parse("10MB"));
        factory.setMaxRequestSize(DataSize.parse("10MB"));
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
}
