package kr.co.govengers.controller;

import kr.co.govengers.dto.ProductDto;
import kr.co.govengers.dto.ProductRegisterRequest;
import kr.co.govengers.entity.Product;
import kr.co.govengers.service.PdSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class PdController {

    private final PdSvc productService;

    // [상품 목록 / 검색] - 페이징 처리
    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String mainCategory,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) String search
    ) {
        Page<ProductDto> list = productService.getProducts(page - 1, size, mainCategory, subCategory, search);
        return ResponseEntity.ok(list);
    }

    // [상세 조회]
    @GetMapping("/{pid}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Integer pid) {
        ProductDto product = productService.getProductById(pid);
        return ResponseEntity.ok(product);
    }

    // [상품 등록] - 이미지 포함
    @PostMapping("/register")
    public ResponseEntity<?> registerProduct(@ModelAttribute ProductRegisterRequest req) {
        System.out.println("Controller에서 인증정보: " +
                SecurityContextHolder.getContext().getAuthentication());
        productService.registerProduct(req, req.getImage());
        return ResponseEntity.ok("상품 등록 완료!");
    }

    // [수정]
    @PutMapping("/{pid}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Integer pid,
            @RequestBody Product updatedProduct
    ) {
        Product updated = productService.updateProduct(pid, updatedProduct);
        return ResponseEntity.ok(updated);
    }

    // [삭제]
    @DeleteMapping("/{pid}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer pid) {
        productService.deleteProduct(pid);
        return ResponseEntity.ok().build();
    }

    // [HIT 표시/해제]
    @PatchMapping("/{pid}/hit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleHit(@PathVariable Integer pid, @RequestBody Map<String, Integer> req) {
        productService.toggleHit(pid, req.get("hit"));
        return ResponseEntity.ok().build();
    }

    // [품절 표시/해제]
    @PatchMapping("/{pid}/soldout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleSoldout(@PathVariable Integer pid, @RequestBody Map<String, Integer> req) {
        productService.toggleSoldout(pid, req.get("soldout"));
        return ResponseEntity.ok().build();
    }
}
