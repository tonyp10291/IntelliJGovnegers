package kr.co.govengers.service;

import kr.co.govengers.dto.ProductDTO;
import kr.co.govengers.dto.ProductRegisterRequest;
import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.enums.AdminStatus;
import kr.co.govengers.entity.enums.MainCategory;
import kr.co.govengers.entity.enums.UserStatus;
import kr.co.govengers.repository.PdRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PdSvc {

    private final PdRepo PdRepo;

    @Value("${custom.upload-path:src/main/resources/static/img}")
    private String uploadDirectory;

    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        return PdRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getProducts(int page, int size, String mainCategory, String search) {
        Pageable pageable = PageRequest.of(page, size);

        List<Product> all = PdRepo.findAll();
        List<ProductDTO> filtered = all.stream()
                .filter(p -> mainCategory == null || mainCategory.isBlank() ||
                        (p.getMainCategory() != null && p.getMainCategory().name().equals(mainCategory)))
                .filter(p -> search == null || search.isBlank() ||
                        (p.getPnm() != null && p.getPnm().toLowerCase().contains(search.toLowerCase())))
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        List<ProductDTO> pageList = filtered.subList(start, end);

        return new PageImpl<>(pageList, pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return PdRepo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getAvailableProducts(Pageable pageable) {
        return PdRepo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductEntityById(Integer pid) {
        return PdRepo.findById(pid);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Integer pid) {
        Product product = PdRepo.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + pid));

        try {
            increaseHitSafely(pid);
        } catch (Exception e) {
            log.warn("조회수 증가 실패 (상품 조회는 정상 진행): {}", e.getMessage());
        }

        return convertToDTO(product);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increaseHitSafely(Integer pid) {
        try {
            Product product = PdRepo.findById(pid).orElse(null);
            if (product != null) {
                product.setHit(product.getHit() + 1);
                PdRepo.save(product);
            }
        } catch (Exception e) {
            log.warn("조회수 증가 실패: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(MainCategory category) {
        return PdRepo.findByMainCategory(category);
    }

    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        return PdRepo.findByPnmContainingIgnoreCase(keyword);
    }

    @Transactional(readOnly = true)
    public List<Product> searchProductsWithCategory(String keyword, MainCategory category) {
        return PdRepo.findByPnmContainingIgnoreCaseAndMainCategory(keyword, category);
    }

    @Transactional(readOnly = true)
    public List<Product> getPopularProducts() {
        return PdRepo.findAllByOrderByHitDesc();
    }

    @Transactional(readOnly = true)
    public List<Product> getLatestProducts() {
        return PdRepo.findAllByOrderByPidDesc();
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceRange(int minPrice, int maxPrice) {
        return PdRepo.findByPriceBetween(minPrice, maxPrice);
    }

    public Product registerProduct(ProductRegisterRequest req, MultipartFile imageFile) {
        String savedFilename = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                savedFilename = saveImageFile(imageFile);
            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 실패: " + e.getMessage(), e);
            }
        }

        MainCategory mainCategory = MainCategory.valueOf(req.getMainCategory());
        LocalDate expDate = null;

        if (req.getExpDate() != null && !req.getExpDate().isBlank()) {
            expDate = LocalDate.parse(req.getExpDate());
        }

        Product product = Product.builder()
                .pnm(req.getPnm())
                .mainCategory(mainCategory)
                .price(req.getPrice())
                .pdesc(req.getPdesc())
                .origin(req.getOrigin())
                .expDate(expDate)
                .hit(req.getHit() != null ? req.getHit() : 0)
                .image(savedFilename)
                .soldout(req.getSoldout() != null ? req.getSoldout() : 0)
                .build();

        return PdRepo.save(product);
    }

    @Transactional
    public Product updateProduct(Integer pid, Product updatedProduct) {
        Product product = PdRepo.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + pid));

        if (updatedProduct.getPnm() != null) {
            product.setPnm(updatedProduct.getPnm());
        }
        if (updatedProduct.getMainCategory() != null) {
            product.setMainCategory(updatedProduct.getMainCategory());
        }
        if (updatedProduct.getPrice() != null) {
            product.setPrice(updatedProduct.getPrice());
        }
        if (updatedProduct.getPdesc() != null) {
            product.setPdesc(updatedProduct.getPdesc());
        }
        if (updatedProduct.getOrigin() != null) {
            product.setOrigin(updatedProduct.getOrigin());
        }
        if (updatedProduct.getExpDate() != null) {
            product.setExpDate(updatedProduct.getExpDate());
        }
        if (updatedProduct.getHit() != null) {
            product.setHit(updatedProduct.getHit());
        }
        if (updatedProduct.getSoldout() != null) {
            product.setSoldout(updatedProduct.getSoldout());
        }
        if (updatedProduct.getImage() != null) {
            product.setImage(updatedProduct.getImage());
        }

        return PdRepo.save(product);
    }

    public Product saveProduct(Product product) {
        if (product.getPid() == null) {
            product.setHit(product.getHit() == null ? 0 : product.getHit());
            product.setSoldout(product.getSoldout() == null ? 0 : product.getSoldout());
        }
        return PdRepo.save(product);
    }

    public void deleteProduct(Integer pid) {
        Product product = PdRepo.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + pid));

        String imgFilename = product.getImage();
        if (imgFilename != null && !imgFilename.isEmpty()) {
            deleteImageFile(imgFilename);
        }

        PdRepo.deleteById(pid);
    }

    public String saveProductImage(Integer pid, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("업로드할 파일이 없습니다.");
        }

        String savedFilename = saveImageFile(file);
        Product product = PdRepo.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + pid));
        product.setImage(savedFilename);
        PdRepo.save(product);
        return savedFilename;
    }

    public void toggleHit(Integer pid, Integer hit) {
        Product product = PdRepo.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + pid));

        product.setHit(hit != null && hit == 1 ? 1 : 0);
        PdRepo.save(product);
    }

    public void toggleSoldout(Integer pid, Integer soldout) {
        Product product = PdRepo.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + pid));

        product.setSoldout(soldout != null && soldout == 1 ? 1 : 0);
        PdRepo.save(product);
    }

    @Transactional
    public void increaseHit(Integer pid) {
        Product product = PdRepo.findById(pid).orElse(null);
        if (product != null) {
            product.setHit(product.getHit() + 1);
            PdRepo.save(product);
        }
    }

    @Transactional
    public Optional<Product> updatePrice(Integer pid, int price) {
        Product product = PdRepo.findById(pid).orElse(null);
        if (product != null) {
            product.setPrice(price);
            PdRepo.save(product);
            return Optional.of(product);
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsWithExpiringDate(int days) {
        LocalDate targetDate = LocalDate.now().plusDays(days);
        return PdRepo.findProductsWithExpiringDate(targetDate);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByOrigin(String origin) {
        return PdRepo.findByOrigin(origin);
    }

    @Transactional(readOnly = true)
    public long getTotalProductCount() {
        return PdRepo.countAvailableProducts();
    }

    @Transactional(readOnly = true)
    public long getProductCountByCategory(MainCategory category) {
        return PdRepo.countByCategoryAndAvailable(category);
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProductsPaging(int page, int size, String mainCategory, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("pid").descending());

        if ((mainCategory == null || mainCategory.isBlank()) &&
                (search == null || search.isBlank())) {
            return PdRepo.findAll(pageable);
        } else if (mainCategory != null && !mainCategory.isBlank() &&
                (search == null || search.isBlank())) {
            return PdRepo.findByMainCategory(MainCategory.valueOf(mainCategory), pageable);
        } else if (search != null && !search.isBlank()) {
            return PdRepo.findByPnmContainingIgnoreCase(search, pageable);
        }

        return PdRepo.findAll(pageable);
    }

    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .pid(product.getPid())
                .pnm(product.getPnm())
                .mainCategory(product.getMainCategory() != null ? product.getMainCategory().name() : null)
                .price(product.getPrice())
                .pdesc(product.getPdesc())
                .origin(product.getOrigin())
                .expDate(product.getExpDate())
                .hit(product.getHit())
                .soldout(product.getSoldout())
                .image(product.getImage())
                .build();
    }

    public String saveImageFile(MultipartFile file) throws IOException {
        File uploadDir = new File(uploadDirectory);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFilename = UUID.randomUUID().toString() + extension;

        Path path = Paths.get(uploadDirectory, savedFilename);
        Files.copy(file.getInputStream(), path);

        return savedFilename;
    }

    private void deleteImageFile(String filename) {
        try {
            Path path = Paths.get(uploadDirectory, filename);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("이미지 파일 삭제 실패: {}", e.getMessage());
        }
    }
}