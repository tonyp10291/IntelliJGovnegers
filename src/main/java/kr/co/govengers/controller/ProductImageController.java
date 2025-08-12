// src/main/java/kr/co/govengers/controller/ProductImageController.java
package kr.co.govengers.controller;

import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.ProductImage;
import kr.co.govengers.repository.ProductImageRepo;
import kr.co.govengers.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductImageController {

    private final ProductRepo productRepo;
    private final ProductImageRepo productImageRepo;

    // ✅ WebConfig와 동일 기본값
    @Value("${custom.upload-path:C:/gogiImage}")
    private String rootDir;

    /** 상세 이미지 업로드 (다중) — 관리자 전용 */
    @PostMapping("/{pid}/images/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> upload(
            @PathVariable Integer pid,
            @RequestParam("files") List<MultipartFile> files
    ) throws IOException {

        Product product = productRepo.findById(pid).orElseThrow();
        Path dir = Paths.get(rootDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        List<String> saved = new ArrayList<>();
        for (MultipartFile mf : files) {
            if (mf == null || mf.isEmpty()) continue;

            String ext = Optional.ofNullable(StringUtils.getFilenameExtension(mf.getOriginalFilename()))
                    .map(e -> "." + e).orElse("");
            String unique = System.currentTimeMillis() + "_" + UUID.randomUUID() + ext;
            Path target = dir.resolve(unique).normalize();

            Files.copy(mf.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            productImageRepo.save(ProductImage.builder()
                    .product(product)
                    .filename(unique)
                    .originalName(mf.getOriginalFilename())
                    .kind("DETAIL")
                    .build());

            saved.add(unique);
        }
        return ResponseEntity.ok(saved);
    }

    /** 상세 이미지 파일명 목록 — 공개 */
    @PostMapping("/{pid}/images/list")
    public ResponseEntity<List<String>> list(@PathVariable Integer pid) {
        List<String> names = productImageRepo.findByProduct_PidOrderByIdAsc(pid)
                .stream().map(ProductImage::getFilename).toList();
        return ResponseEntity.ok(names);
    }
}
