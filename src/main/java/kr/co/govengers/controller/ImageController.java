package kr.co.govengers.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
@Slf4j
public class ImageController {

    @Value("${custom.upload-path}")
    private String uploadPath;

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            log.info("=== 이미지 서빙 요청 ===");
            log.info("요청된 파일명: {}", filename);
            log.info("업로드 경로: {}", uploadPath);

            Path filePath = Paths.get(uploadPath).resolve(filename);
            log.info("전체 파일 경로: {}", filePath.toString());

            Resource resource = new UrlResource(filePath.toUri());
            log.info("리소스 존재: {}", resource.exists());
            log.info("리소스 읽기 가능: {}", resource.isReadable());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("파일을 찾을 수 없거나 읽을 수 없음: {}", filename);
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "image/jpeg";
            }
            log.info("컨텐츠 타입: {}", contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            log.error("이미지 서빙 에러: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}