package kr.co.govengers.controller;

import kr.co.govengers.entity.Product;
import kr.co.govengers.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UPicController {

    private final ProductRepository productRepository;

    @PostMapping("/wishlist")
    public ResponseEntity<List<Product>> getProducts() {

    }

    @PostMapping("/wishlist/user")
}
