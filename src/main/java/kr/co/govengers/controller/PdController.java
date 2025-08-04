package kr.co.govengers.controller;

import kr.co.govengers.dto.ProductDto;
import kr.co.govengers.dto.ProductRegisterRequest;
import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.User;
import kr.co.govengers.entity.enums.MainCategory;
import kr.co.govengers.service.PdSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class PdController {

    private final PdSvc pdSvc;

    @GetMapping
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
            errorResponse.put("content", new java.util.ArrayList<>());
            errorResponse.put("totalPages", 0);
            errorResponse.put("totalElements", 0);
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<Product>> getAllProductsList() {
        List<Product> products = pdSvc.getProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{pid}")
    public ResponseEntity<ProductDto> getProductById(
            @PathVariable Integer pid,
            @AuthenticationPrincipal User user
    ) {
        System.out.println("=== 관리자 개별 상품 조회 ===");
        System.out.println("상품 ID: " + pid);
        System.out.println("현재 사용자: " + (user != null ? user.getUid() + " / " + user.getRole() : "null"));

        try {
            ProductDto product = pdSvc.getProductById(pid);
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

    @GetMapping("/{pid}/entity")
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

            pdSvc.registerProduct(request, request.getImage());

            response.put("success", true);
            response.put("message", "상품이 성공적으로 등록되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("상품 등록 중 에러: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "상품 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping
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