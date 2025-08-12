package kr.co.govengers.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
@Slf4j
public class ImageController {

    @Value("${custom.upload-path}")
    private String uploadPath;

    @Value("${custom.default-image:default-product.jpg}")
    private String defaultImage;

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path base = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path file = base.resolve(filename).normalize();

            log.info("=== 이미지 서빙 요청 === filename={}, base={}, file={}", filename, base, file);

            if (!file.startsWith(base) || !Files.exists(file) || !Files.isReadable(file)) {
                log.warn("요청 파일 없음/읽기불가 → 폴백 시도: {}", filename);
                Path fallback = base.resolve(defaultImage).normalize();
                if (fallback.startsWith(base) && Files.exists(fallback) && Files.isReadable(fallback)) {
                    Resource fb = new UrlResource(fallback.toUri());
                    MediaType fbType = MediaTypeFactory.getMediaType(fb)
                            .orElse(MediaType.IMAGE_JPEG);
                    return ResponseEntity.ok().contentType(fbType).body(fb);
                }
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(file.toUri());
            MediaType type = MediaTypeFactory.getMediaType(resource)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);


            return ResponseEntity.ok()
                    .contentType(type)
                    .body(resource);

        } catch (Exception e) {
            log.error("이미지 서빙 에러 ({}): {}", filename, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
