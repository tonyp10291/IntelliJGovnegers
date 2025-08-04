package kr.co.govengers.controller;

import kr.co.govengers.dto.ProductDto;
import kr.co.govengers.dto.ProductRegisterRequest;
import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.User;
import kr.co.govengers.entity.enums.MainCategory;
import kr.co.govengers.service.PdSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class PdController {

    private final PdSvc pdSvc;

    @GetMapping("/pageable")
    public ResponseEntity<Page<Product>> getAllProductsWithPageable(
            @PageableDefault(size = 12, sort = "pid", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Product> products = pdSvc.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Product>> getAllProductsList() {
        List<Product> products = pdSvc.getProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{pid}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Integer pid) {
        try {
            ProductDto product = pdSvc.getProductById(pid);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{pid}/entity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> getProductEntity(@PathVariable Integer pid) {
        ProductDto product = pdSvc.getProductById(pid);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable MainCategory category) {
        List<Product> products = pdSvc.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        List<Product> products = pdSvc.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Product>> getPopularProducts() {
        List<Product> products = pdSvc.getPopularProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Product>> getLatestProducts() {
        List<Product> products = pdSvc.getLatestProducts();
        return ResponseEntity.ok(products);
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> registerProduct(
            @ModelAttribute ProductRegisterRequest request,
            @AuthenticationPrincipal User user
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (user == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            pdSvc.registerProduct(request, request.getImage());

            response.put("success", true);
            response.put("message", "상품이 성공적으로 등록되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "상품 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createProduct(
            @RequestBody Product product,
            @AuthenticationPrincipal User user
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            Product savedProduct = pdSvc.saveProduct(product);
            response.put("success", true);
            response.put("message", "상품이 생성되었습니다.");
            response.put("product", savedProduct);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "상품 생성 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/{pid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable Integer pid,
            @RequestBody Product updatedProduct,
            @AuthenticationPrincipal User user
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            Product updated = pdSvc.updateProduct(pid, updatedProduct);
            response.put("success", true);
            response.put("message", "상품이 수정되었습니다.");
            response.put("product", updated);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "상품 수정 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/{pid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteProduct(
            @PathVariable Integer pid,
            @AuthenticationPrincipal User user
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            pdSvc.deleteProduct(pid);
            response.put("success", true);
            response.put("message", "상품이 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "상품 삭제 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/{pid}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadProductImage(
            @PathVariable Integer pid,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            String filename = pdSvc.saveProductImage(pid, file);
            response.put("success", true);
            response.put("message", "이미지가 업로드되었습니다.");
            response.put("filename", filename);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "이미지 업로드 실패: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    @PatchMapping("/{pid}/hit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleHit(
            @PathVariable Integer pid,
            @RequestBody Map<String, Integer> request,
            @AuthenticationPrincipal User user
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            pdSvc.toggleHit(pid, request.get("hit"));
            response.put("success", true);
            response.put("message", "HIT 상태가 변경되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "HIT 상태 변경 실패: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    @PatchMapping("/{pid}/soldout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleSoldout(
            @PathVariable Integer pid,
            @RequestBody Map<String, Integer> request,
            @AuthenticationPrincipal User user
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            pdSvc.toggleSoldout(pid, request.get("soldout"));
            response.put("success", true);
            response.put("message", "품절 상태가 변경되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "품절 상태 변경 실패: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    @PutMapping("/{pid}/price")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updatePrice(
            @PathVariable Integer pid,
            @RequestParam int price,
            @AuthenticationPrincipal User user
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Product> product = pdSvc.updatePrice(pid, price);
            if (product.isPresent()) {
                response.put("success", true);
                response.put("message", "가격이 업데이트되었습니다.");
                response.put("product", product.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "상품을 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "가격 업데이트 실패: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<MainCategory[]> getCategories() {
        return ResponseEntity.ok(MainCategory.values());
    }
}