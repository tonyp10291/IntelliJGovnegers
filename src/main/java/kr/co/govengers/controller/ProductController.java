package kr.co.govengers.controller;

import kr.co.govengers.dto.ProductRegisterRequest;
import kr.co.govengers.entity.Product;
import kr.co.govengers.service.PdSvs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final PdSvs productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> list = productService.getProducts();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{pid}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer pid, @RequestBody Product updatedProduct) {
        Product updated = productService.updateProduct(pid, updatedProduct);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerProduct(
            @ModelAttribute ProductRegisterRequest req,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        productService.registerProduct(req, imageFile);
        return ResponseEntity.ok("상품 등록 완료!");
    }
}
