package kr.co.govengers.controller;

import kr.co.govengers.dto.ProductRegisterRequest;
import kr.co.govengers.entity.Product;
import kr.co.govengers.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getProducts();
    }

    @PutMapping("/{pid}")
    public Product updateProduct(@PathVariable Integer pid, @RequestBody Product updatedProduct) {
        return productService.updateProduct(pid, updatedProduct);
    }

    // 권한 어노테이션
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @PostMapping("/register")
    public ResponseEntity<?> registerProduct(@ModelAttribute ProductRegisterRequest request) {
        productService.registerProduct(request);
        return ResponseEntity.ok().body("상품 등록 성공");
    }
}
