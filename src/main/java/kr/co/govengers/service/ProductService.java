package kr.co.govengers.service;

import kr.co.govengers.dto.ProductRegisterRequest;
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
        product.setImageUrl(updatedProduct.getImageUrl()); // imageUrl 필드 추가시
        return product;
    }

    public void registerProduct(ProductRegisterRequest req) {
        String imageUrl = null;
        if (req.getImage() != null && !req.getImage().isEmpty()) {
            // 실제 이미지 저장 구현 필요 (예: S3, 서버경로, etc)
            imageUrl = "/img/" + req.getImage().getOriginalFilename(); // 임시 예시
        }

        Product product = Product.builder()
                .pnm(req.getPnm())
                .mainCategory(req.getMainCategory())
                .subCategory(req.getSubCategory())
                .price(req.getPrice())
                .pdesc(req.getPdesc())
                .origin(req.getOrigin())
                .expDate(req.getExpDate())
                .hit(0) // 신규 등록은 0
                .userStatus(req.getUserStatus())
                .adminStatus(req.getAdminStatus())
                .imageUrl(imageUrl)
                .build();
        productRepository.save(product);
    }
}
