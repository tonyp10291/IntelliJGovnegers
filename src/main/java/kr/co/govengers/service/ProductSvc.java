package kr.co.govengers.service;

import kr.co.govengers.dto.ProductRegisterRequest;
import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.enums.MainCategory;
import kr.co.govengers.entity.enums.SubCategory;
import kr.co.govengers.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductSvc {

    private final ProductRepo productRepo;

    @Value("${custom.upload-path}")
    private String uploadPath;

    // 1번 파일의 상품 목록 조회 기능
    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        return productRepo.findAll();
    }

    // 2번 파일의 이미지 업로드가 포함된 상품 등록 기능
    @Transactional
    public void registerProduct(ProductRegisterRequest req, MultipartFile imageFile) throws IOException {

        String savedFilename = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            String originalFilename = imageFile.getOriginalFilename();
            savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            File dest = new File(uploadPath, savedFilename);
            imageFile.transferTo(dest);
        }

        Product product = Product.builder()
                .pnm(req.getName())
                .mainCategory(MainCategory.valueOf(req.getMainCategory()))
                .subCategory(SubCategory.valueOf(req.getSubCategory()))
                .price(req.getPrice())
                .pdesc(req.getDescription())
                .stock(req.getStock())
                .hit(req.getHit())
                .imgFilename(savedFilename)
                .build();

        productRepo.save(product);
    }

    // 1번 파일의 상품 수정 기능
    @Transactional
    public Product updateProduct(Integer pid, Product updatedProduct) {
        Product product = productRepo.findById(pid)
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
        return productRepo.save(product);
    }
}