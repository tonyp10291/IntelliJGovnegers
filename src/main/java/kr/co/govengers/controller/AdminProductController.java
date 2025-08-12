// src/main/java/kr/co/govengers/controller/AdminProductController.java
package kr.co.govengers.controller;

import kr.co.govengers.dto.ProductRegisterRequest;
import kr.co.govengers.service.PdSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final PdSvc pdSvc;

    // 대표이미지 포함 상품 등록 (프론트: FormData에 pnm, mainCategory, ... + image)
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> register(
            @ModelAttribute ProductRegisterRequest req,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        Integer pid = pdSvc.createReturnPid(req, image);

        return ResponseEntity
                .created(URI.create("/api/products/" + pid)) // Location 헤더
                .body(Map.of(
                        "pid", pid,
                        "success", true,
                        "message", "상품이 성공적으로 등록되었습니다."
                ));
    }

    // 상세이미지 업로드 (프론트: /api/products/{pid}/images 로 files[] 전송)
    @PostMapping(value = "/{pid}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadDetailImages(
            @PathVariable Integer pid,
            @RequestPart("files") List<MultipartFile> files
    ) {
        var saved = pdSvc.saveDetailImages(pid, files);
        return ResponseEntity.ok(Map.of(
                "count", saved.size(),
                "paths", saved
        ));
    }

    // 상세이미지 목록
    @GetMapping("/{pid}/images")
    public ResponseEntity<List<String>> listDetailImages(@PathVariable Integer pid) {
        return ResponseEntity.ok(pdSvc.listDetailImages(pid));
    }

    // 상세이미지 개별 삭제 (filename은 저장된 실제 파일명)
    @DeleteMapping("/{pid}/images/{filename}")
    public ResponseEntity<Void> deleteDetailImage(
            @PathVariable Integer pid,
            @PathVariable String filename
    ) {
        pdSvc.deleteDetailImage(pid, filename);
        return ResponseEntity.noContent().build();
    }
}
