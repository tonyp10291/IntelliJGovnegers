package kr.co.govengers.service;

import kr.co.govengers.dto.ProductDto;
import kr.co.govengers.dto.ProductRegisterRequest;
import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.enums.AdminStatus;
import kr.co.govengers.entity.enums.MainCategory;
import kr.co.govengers.entity.enums.SubCategory;
import kr.co.govengers.entity.enums.UserStatus;
import kr.co.govengers.repository.PdRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdSvc {

    private final PdRepo productRepository;

    @Value("${custom.upload-path}")
    private String uploadPath;

    // ✅ 상품 등록
    @Transactional
    public void registerProduct(ProductRegisterRequest req, MultipartFile imageFile) {
        String savedFilename = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            String originalFilename = imageFile.getOriginalFilename();
            savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;

            File folder = new File(uploadPath);
            if (!folder.exists()) folder.mkdirs();

            File dest = new File(uploadPath, savedFilename);
            try {
                imageFile.transferTo(dest);
            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 실패!", e);
            }
        }

        LocalDate expDate = null;
        if (req.getExpDate() != null && !req.getExpDate().isBlank()) {
            expDate = LocalDate.parse(req.getExpDate().trim());
        }

        Product product = Product.builder()
                .pnm(req.getPnm())
                .mainCategory(MainCategory.valueOf(req.getMainCategory().trim()))
                .subCategory(SubCategory.valueOf(req.getSubCategory().trim()))
                .price(req.getPrice())
                .pdesc(req.getPdesc())
                .origin(req.getOrigin())
                .expDate(expDate)
                .userStatus(UserStatus.valueOf(req.getUserStatus().trim()))
                .adminStatus(AdminStatus.valueOf(req.getAdminStatus().trim()))
                .hit(req.getHit())
                .soldout(req.getSoldout())
                .image(savedFilename)
                .build();

        productRepository.save(product);
    }

    // ✅ 상품 목록 - 페이징 + 검색 필터
    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(int page, int size, String mainCategory, String subCategory, String search) {
        Pageable pageable = PageRequest.of(page, size);

        List<ProductDto> filtered = productRepository.findAll().stream()
                .filter(p -> mainCategory == null || mainCategory.isBlank() || (p.getMainCategory() != null && p.getMainCategory().name().equals(mainCategory)))
                .filter(p -> subCategory == null || subCategory.isBlank() || (p.getSubCategory() != null && p.getSubCategory().equals(subCategory)))
                .filter(p -> search == null || search.isBlank() || (p.getPnm() != null && p.getPnm().toLowerCase().contains(search.toLowerCase())))
                .map(this::toDto)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<ProductDto> pageList = filtered.subList(start, end);

        return new PageImpl<>(pageList, pageable, filtered.size());
    }

    // ✅ 단건 조회 (DTO)
    @Transactional(readOnly = true)
    public ProductDto getProductById(Integer pid) {
        Product p = getProductEntityById(pid);
        return toDto(p);
    }

    // ✅ 상품 수정
    @Transactional
    public Product updateProduct(Integer pid, Product updatedProduct) {
        Product product = getProductEntityById(pid);
        product.setPnm(updatedProduct.getPnm());
        product.setMainCategory(updatedProduct.getMainCategory());
        product.setSubCategory(updatedProduct.getSubCategory());
        product.setPrice(updatedProduct.getPrice());
        product.setPdesc(updatedProduct.getPdesc());
        product.setOrigin(updatedProduct.getOrigin());
        product.setExpDate(updatedProduct.getExpDate());
        product.setUserStatus(updatedProduct.getUserStatus());
        product.setAdminStatus(updatedProduct.getAdminStatus());
        product.setHit(updatedProduct.getHit());
        product.setSoldout(updatedProduct.getSoldout());
        product.setImage(updatedProduct.getImage());
        return productRepository.save(product);
    }

    // ✅ 상품 삭제
    @Transactional
    public void deleteProduct(Integer pid) {
        if (!productRepository.existsById(pid)) {
            throw new RuntimeException("존재하지 않는 상품입니다.");
        }
        productRepository.deleteById(pid);
    }

    // ✅ HIT 토글
    @Transactional
    public void toggleHit(Integer pid, int hit) {
        Product product = getProductEntityById(pid);
        product.setHit(hit == 1 ? 1 : 0);
        productRepository.save(product);
    }

    // ✅ 품절 토글
    @Transactional
    public void toggleSoldout(Integer pid, int soldout) {
        Product product = getProductEntityById(pid);
        product.setSoldout(soldout == 1 ? 1 : 0);
        productRepository.save(product);
    }

    // 🔐 Entity 단건 조회 내부 메서드
    private Product getProductEntityById(Integer pid) {
        return productRepository.findById(pid)
                .orElseThrow(() -> new RuntimeException("상품 없음: " + pid));
    }

    // 🔁 Entity → DTO 변환 메서드
    private ProductDto toDto(Product p) {
        return ProductDto.builder()
                .pid(p.getPid())
                .pnm(p.getPnm())
                .mainCategory(p.getMainCategory() != null ? p.getMainCategory().name() : null)
                .subCategory(p.getSubCategory() != null ? p.getSubCategory().name() : null)
                .price(p.getPrice())
                .pdesc(p.getPdesc())
                .origin(p.getOrigin())
                .expDate(p.getExpDate())
                .soldout(p.getSoldout())
                .image(p.getImage())
                .hit(p.getHit())
                .build();
    }
}
