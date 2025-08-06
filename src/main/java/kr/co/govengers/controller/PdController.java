package kr.co.govengers.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.co.govengers.dto.ProductDTO;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class PdController {

    private final PdSvc pdSvc;

    @GetMapping("/api/products/list")
    public ResponseEntity<List<Product>> getAllProductsList() {
        List<Product> products = pdSvc.getProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/api/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = pdSvc.getProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/api/products/pageable")
    public ResponseEntity<Page<Product>> getAllProductsWithPageable(
            @PageableDefault(size = 12, sort = "pid", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Product> products = pdSvc.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/api/products/{pid}")
    public ResponseEntity<ProductDTO> getProductById(
            @PathVariable Integer pid,
            @AuthenticationPrincipal User user
    ) {
        try {
            ProductDTO product = pdSvc.getProductById(pid);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/api/products/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable MainCategory category) {
        List<Product> products = pdSvc.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/api/products/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        List<Product> products = pdSvc.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/api/products/popular")
    public ResponseEntity<List<Product>> getPopularProducts() {
        List<Product> products = pdSvc.getPopularProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/api/products/latest")
    public ResponseEntity<List<Product>> getLatestProducts() {
        List<Product> products = pdSvc.getLatestProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/api/products/categories")
    public ResponseEntity<MainCategory[]> getCategories() {
        return ResponseEntity.ok(MainCategory.values());
    }

    @GetMapping("/api/admin/products/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Product>> getAllProductsListAdmin() {
        List<Product> products = pdSvc.getProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/api/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Product>> getAllProductsAdmin() {
        List<Product> products = pdSvc.getProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/api/admin/products/paging")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllProductsWithPaging(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String mainCategory,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal User user,
            Authentication authentication
    ) {
        System.out.println("=== 관리자 상품 목록 조회 ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("현재 사용자: " + (user != null ? user.getUid() + " / " + user.getRole() : "null"));
        System.out.println("페이지: " + page + ", 사이즈: " + size);
        System.out.println("카테고리: " + mainCategory + ", 검색어: " + search);

        try {
            Page<Product> result = pdSvc.getAllProductsPaging(page - 1, size, mainCategory, search);

            Map<String, Object> response = new HashMap<>();
            response.put("content", result.getContent());
            response.put("totalPages", result.getTotalPages());
            response.put("totalElements", result.getTotalElements());
            response.put("currentPage", page);
            response.put("size", size);
            response.put("numberOfElements", result.getNumberOfElements());

            System.out.println("조회된 상품 수: " + result.getContent().size());
            System.out.println("총 페이지 수: " + result.getTotalPages());
            System.out.println("==============================");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("상품 목록 조회 중 에러: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("content", new ArrayList<>());
            errorResponse.put("totalPages", 0);
            errorResponse.put("totalElements", 0);
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/api/admin/products/{pid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> getProductByIdAdmin(
            @PathVariable Integer pid,
            @AuthenticationPrincipal User user
    ) {
        System.out.println("=== 관리자 개별 상품 조회 ===");
        System.out.println("상품 ID: " + pid);
        System.out.println("현재 사용자: " + (user != null ? user.getUid() + " / " + user.getRole() : "null"));

        try {
            ProductDTO product = pdSvc.getProductById(pid);
            System.out.println("조회된 상품: " + product.getPnm());
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            System.out.println("상품을 찾을 수 없음: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.out.println("상품 조회 중 에러: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/api/admin/products/{pid}/entity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> getProductEntity(@PathVariable Integer pid) {
        ProductDTO product = pdSvc.getProductById(pid);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/api/admin/products/category/{category}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Product>> getProductsByCategoryAdmin(@PathVariable MainCategory category) {
        List<Product> products = pdSvc.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/api/admin/products/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Product>> searchProductsAdmin(@RequestParam String keyword) {
        List<Product> products = pdSvc.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/api/admin/products/popular")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Product>> getPopularProductsAdmin() {
        List<Product> products = pdSvc.getPopularProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/api/admin/products/latest")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Product>> getLatestProductsAdmin() {
        List<Product> products = pdSvc.getLatestProducts();
        return ResponseEntity.ok(products);
    }

    @PostMapping("/api/admin/products/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> registerProduct(
            @ModelAttribute ProductRegisterRequest request,
            @AuthenticationPrincipal User user,
            Authentication authentication
    ) {
        Map<String, Object> response = new HashMap<>();

        System.out.println("=== 관리자 상품 등록 컨트롤러 ===");
        System.out.println("Authentication 객체: " + authentication);
        System.out.println("현재 사용자: " + (authentication != null ? authentication.getName() : "없음"));
        System.out.println("현재 권한: " + (authentication != null ? authentication.getAuthorities() : "없음"));
        System.out.println("인증 여부: " + (authentication != null ? authentication.isAuthenticated() : false));
        System.out.println("@AuthenticationPrincipal User: " + (user != null ? user.getUid() + " / " + user.getRole() : "null"));
        System.out.println("요청 데이터: " + request);
        System.out.println("================================");

        try {
            if (user == null) {
                System.out.println("사용자가 null입니다!");
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            if (!"ROLE_ADMIN".equals(user.getRole())) {
                System.out.println("관리자 권한이 없습니다. 현재 권한: " + user.getRole());
                response.put("success", false);
                response.put("message", "관리자 권한이 필요합니다.");
                return ResponseEntity.status(403).body(response);
            }

            Product registeredProduct = pdSvc.registerProduct(request, request.getImage());

            response.put("success", true);
            response.put("message", "상품이 성공적으로 등록되었습니다.");
            response.put("product", registeredProduct);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("상품 등록 중 에러: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "상품 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/api/admin/products")
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

    @PostMapping("/api/admin/products/{pid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateProductWithFile(
            @PathVariable Integer pid,
            @RequestParam("productData") String productData,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal User user,
            Authentication authentication
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        Product product = null;
        try {
            product = objectMapper.readValue(productData, Product.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        System.out.println("=== 상품 수정 요청 ===");
        System.out.println("상품 ID: " + pid);
        System.out.println("사용자: " + (user != null ? user.getUid() + " / " + user.getRole() : "null"));
        System.out.println("파일: " + (file != null ? file.getOriginalFilename() : "없음"));
        System.out.println("상품 데이터: " + product);

        Map<String, Object> response = new HashMap<>();

        try {
            String savedFilename = null;

            if (file != null && !file.isEmpty()) {
                try {
                    savedFilename = pdSvc.saveImageFile(file);
                    product.setImage(savedFilename);
                } catch (IOException e) {
                    throw new RuntimeException("이미지 저장 실패: " + e.getMessage(), e);
                }
            }
            // 파일이 없으면 기존 이미지 유지 (product.setImage() 호출하지 않음)

            Product updated = pdSvc.updateProduct(pid, product);

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

    @PutMapping("/api/admin/products/{pid}")
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

    @DeleteMapping("/api/admin/products/{pid}")
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

    @PostMapping("/api/admin/products/{pid}/image")
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
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "이미지 업로드 실패: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "이미지 업로드 실패: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    @PatchMapping("/api/admin/products/{pid}/hit")
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

    @PatchMapping("/api/admin/products/{pid}/soldout")
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

    @PutMapping("/api/admin/products/{pid}/price")
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

    @GetMapping("/api/admin/products/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainCategory[]> getCategoriesAdmin() {
        return ResponseEntity.ok(MainCategory.values());
    }
}