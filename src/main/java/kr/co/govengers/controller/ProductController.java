package kr.co.govengers.controller;

import kr.co.govengers.dto.ProductRegisterRequest;
import kr.co.govengers.entity.Product;
import kr.co.govengers.service.PdSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final PdSvc productSvc;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> list = productSvc.getProducts();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{pid}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer pid, @RequestBody Product updatedProduct) {
        Product updated = productSvc.updateProduct(pid, updatedProduct);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerProduct(
            @RequestPart("product") ProductRegisterRequest request,
            @RequestPart("image") MultipartFile imageFile) {
        try {
            productSvc.registerProduct(request, imageFile);
            return ResponseEntity.ok("상품이 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("상품 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}