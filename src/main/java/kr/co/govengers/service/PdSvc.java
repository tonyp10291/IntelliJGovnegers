package kr.co.govengers.service;

import kr.co.govengers.dto.ProductDto;
import kr.co.govengers.dto.ProductRegisterRequest;
import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.enums.MainCategory;
import kr.co.govengers.repository.PdRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final PdRepo pdRepository;

    @Value("${custom.upload-path:src/main/resources/static/img}")
    private String uploadDirectory;

    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        return pdRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(int page, int size, String mainCategory, String search) {
        Pageable pageable = PageRequest.of(page, size);

        List<Product> all = pdRepository.findAll();
        List<ProductDto> filtered = all.stream()
                .filter(p -> mainCategory == null || mainCategory.isBlank() ||
                        (p.getMainCategory() != null && p.getMainCategory().name().equals(mainCategory)))
                .filter(p -> search == null || search.isBlank() ||
                        (p.getPnm() != null && p.getPnm().toLowerCase().contains(search.toLowerCase())))
                .map(this::convertToDto)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        List<ProductDto> pageList = filtered.subList(start, end);

        return new PageImpl<>(pageList, pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return pdRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getAvailableProducts(Pageable pageable) {
        return pdRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductEntityById(Integer pid) {
        return pdRepository.findById(pid);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Integer pid) {
        Product product = pdRepository.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + pid));

        increaseHit(pid);
        return convertToDto(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(MainCategory category) {
        return pdRepository.findByMainCategory(category);
    }

    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        return pdRepository.findByPnmContainingIgnoreCase(keyword);
    }

    @Transactional(readOnly = true)
    public List<Product> searchProductsWithCategory(String keyword, MainCategory category) {
        return pdRepository.findByPnmContainingIgnoreCaseAndMainCategory(keyword, category);
    }

    @Transactional(readOnly = true)
    public List<Product> getPopularProducts() {
        return pdRepository.findAllByOrderByHitDesc();
    }

    @Transactional(readOnly = true)
    public List<Product> getLatestProducts() {
        return pdRepository.findAllByOrderByPidDesc();
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceRange(int minPrice, int maxPrice) {
        return pdRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public void registerProduct(ProductRegisterRequest req, MultipartFile imageFile) {
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
                .soldout(0) // 기본값 0 (판매중)
                .build();

        pdRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Integer pid, Product updatedProduct) {
        Product product = pdRepository.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + pid));

        product.setPnm(updatedProduct.getPnm());
        product.setMainCategory(updatedProduct.getMainCategory());
        product.setPrice(updatedProduct.getPrice());
        product.setPdesc(updatedProduct.getPdesc());
        product.setOrigin(updatedProduct.getOrigin());
        product.setExpDate(updatedProduct.getExpDate());
        product.setHit(updatedProduct.getHit());

        return pdRepository.save(product);
    }

    public Product saveProduct(Product product) {
        if (product.getPid() == null) {
            product.setHit(product.getHit() == null ? 0 : product.getHit());
            product.setSoldout(product.getSoldout() == null ? 0 : product.getSoldout());
        }
        return pdRepository.save(product);
    }

    public void deleteProduct(Integer pid) {
        Product product = pdRepository.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + pid));

        String imgFilename = product.getImage();
        if (imgFilename != null && !imgFilename.isEmpty()) {
            deleteImageFile(imgFilename);
        }

        pdRepository.deleteById(pid);
    }

    public String saveProductImage(Integer pid, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("업로드할 파일이 없습니다.");
        }

        String savedFilename = saveImageFile(file);
        Product product = pdRepository.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + pid));
        product.setImage(savedFilename);
        pdRepository.save(product);
        return savedFilename;
    }

    public void toggleHit(Integer pid, Integer hit) {
        Product product = pdRepository.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + pid));

        product.setHit(hit != null && hit == 1 ? 1 : 0);
        pdRepository.save(product);
    }

    public void toggleSoldout(Integer pid, Integer soldout) {
        Product product = pdRepository.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + pid));

        product.setSoldout(soldout != null && soldout == 1 ? 1 : 0);
        pdRepository.save(product);
    }

    public void increaseHit(Integer pid) {
        pdRepository.increaseHit(pid);
    }

    public Optional<Product> updatePrice(Integer pid, int price) {
        pdRepository.updatePrice(pid, price);
        return pdRepository.findById(pid);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsWithExpiringDate(int days) {
        LocalDate targetDate = LocalDate.now().plusDays(days);
        return pdRepository.findProductsWithExpiringDate(targetDate);
    }

    @Transactional(readOnly = true)
    public List<Product> getOutOfStockProducts() {
        return pdRepository.findBySoldout(1);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByOrigin(String origin) {
        return pdRepository.findByOrigin(origin);
    }

    @Transactional(readOnly = true)
    public long getTotalProductCount() {
        return pdRepository.countAvailableProducts();
    }

    @Transactional(readOnly = true)
    public long getProductCountByCategory(MainCategory category) {
        return pdRepository.countByCategoryAndAvailable(category);
    }

    private ProductDto convertToDto(Product product) {
        return ProductDto.builder()
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

    private String saveImageFile(MultipartFile file) throws IOException {
        File uploadDir = new File(uploadDirectory);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
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