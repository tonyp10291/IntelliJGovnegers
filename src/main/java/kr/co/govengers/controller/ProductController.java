package kr.co.govengers.controller;

import kr.co.govengers.entity.Product;
import kr.co.govengers.service.ProductService;
import lombok.RequiredArgsConstructor;
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
}
