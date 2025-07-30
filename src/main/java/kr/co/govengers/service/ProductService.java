package kr.co.govengers.service;

import kr.co.govengers.entity.Product;
import kr.co.govengers.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public Product updateProduct(Integer pid, Product updatedProduct) {
        Product product = productRepository.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + pid));
        // 수정가능한 모든 필드 반영 (HIT 포함)
        product.setPnm(updatedProduct.getPnm());
        product.setMainCategory(updatedProduct.getMainCategory());
        product.setSubCategory(updatedProduct.getSubCategory());
        product.setPrice(updatedProduct.getPrice());
        product.setPdesc(updatedProduct.getPdesc());
        product.setOrigin(updatedProduct.getOrigin());
        product.setExpDate(updatedProduct.getExpDate());
        product.setHit(updatedProduct.getHit());
        product.setUserStatus(updatedProduct.getUserStatus());
        product.setAdminStatus(updatedProduct.getAdminStatus());
        return product;
    }
}