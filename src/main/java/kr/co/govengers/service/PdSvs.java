package kr.co.govengers.service;

import kr.co.govengers.dto.ProductRegisterRequest;
import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.enums.MainCategory;
import kr.co.govengers.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PdSvs {

    private final ProductRepository productRepository;

    // 전체 상품 조회
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    // 상품 등록
    @Transactional
    public void registerProduct(ProductRegisterRequest req, MultipartFile imageFile) {
        // TODO: 이미지 저장 로직 추가 (imageFile 사용)
        Product product = Product.builder()
                .pnm(req.getName())
                .mainCategory(MainCategory.valueOf(req.getMainCategory())) // 실제 필드에 맞춰서 조정!
                .price(req.getPrice())
                .pdesc(req.getDescription())
                .hit(req.getHit()) // HIT 값 반영
                .build();
        productRepository.save(product);
        // TODO: 이미지도 product에 저장해야 하면 별도 추가
    }

    // 상품 수정
    @Transactional
    public Product updateProduct(Integer pid, Product updatedProduct) {
        Product product = productRepository.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + pid));
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
