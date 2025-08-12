// src/main/java/kr/co/govengers/service/PdSvc.java
package kr.co.govengers.service;

import kr.co.govengers.dto.ProductDTO;
import kr.co.govengers.dto.ProductRegisterRequest;
import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.enums.MainCategory;
import kr.co.govengers.repository.CartRepo;
import kr.co.govengers.repository.MRvRepo;
import kr.co.govengers.repository.PdRepo;
import kr.co.govengers.repository.UPicRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PdSvc {

    private final PdRepo PdRepo;
    private final CartRepo cartRepo;
    private final MRvRepo mRvRepo;
    private final UPicRepo uPicRepo;

    /**
     * 정적 리소스 루트 (예: src/main/resources/static/img)
     * /api/images/** 가 이 경로로 매핑되어 있다고 가정.
     */
    @Value("${custom.upload-path:src/main/resources/static/img}")
    private String uploadDirectory;

    /* ============================ 조회/검색 ============================ */

    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        return PdRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getProducts(int page, int size, String mainCategory, String search) {
        Pageable pageable = PageRequest.of(page, size);
        List<Product> all = PdRepo.findAll();
        List<ProductDTO> filtered = all.stream()
                .filter(p -> mainCategory == null || mainCategory.isBlank()
                        || (p.getMainCategory() != null && p.getMainCategory().name().equals(mainCategory)))
                .filter(p -> search == null || search.isBlank()
                        || (p.getPnm() != null && p.getPnm().toLowerCase().contains(search.toLowerCase())))
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<ProductDTO> pageList = (start <= end ? filtered.subList(start, end) : List.of());
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
            log.warn("조회수 증가 실패: {}", e.getMessage());
        }
        return convertToDTO(product);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increaseHitSafely(Integer pid) {
        try {
            Product product = PdRepo.findById(pid).orElse(null);
            if (product != null) {
                product.setHit(product.getHit());
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

    /* ============================ 등록/수정 ============================ */

    /**
     * 기존 메서드: 상품 등록 후 Product 반환
     */
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
        LocalDate expDate = (req.getExpDate() != null && !req.getExpDate().isBlank()) ? LocalDate.parse(req.getExpDate()) : null;

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

    /**
     * 컨트롤러에서 Location 헤더 및 pid 본문을 내려주기 위해
     * "pid만 반환"하는 얇은 래퍼.
     */
    public Integer createReturnPid(ProductRegisterRequest req, MultipartFile imageFile) {
        Product saved = registerProduct(req, imageFile);
        return saved.getPid();
    }

    @Transactional
    public Product updateProduct(Integer pid, Product updatedProduct) {
        Product product = PdRepo.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + pid));

        if (updatedProduct.getPnm() != null) product.setPnm(updatedProduct.getPnm());
        if (updatedProduct.getMainCategory() != null) product.setMainCategory(updatedProduct.getMainCategory());
        if (updatedProduct.getPrice() != null) product.setPrice(updatedProduct.getPrice());
        if (updatedProduct.getPdesc() != null) product.setPdesc(updatedProduct.getPdesc());
        if (updatedProduct.getOrigin() != null) product.setOrigin(updatedProduct.getOrigin());
        if (updatedProduct.getExpDate() != null) product.setExpDate(updatedProduct.getExpDate());
        if (updatedProduct.getHit() != null) product.setHit(updatedProduct.getHit());
        if (updatedProduct.getSoldout() != null) product.setSoldout(updatedProduct.getSoldout());
        if (updatedProduct.getImage() != null) product.setImage(updatedProduct.getImage());

        return PdRepo.save(product);
    }

    public Product saveProduct(Product product) {
        if (product.getPid() == null) {
            product.setHit(product.getHit() == null ? 0 : product.getHit());
            product.setSoldout(product.getSoldout() == null ? 0 : product.getSoldout());
        }
        return PdRepo.save(product);
    }

    /* ============================ 삭제 ============================ */

    @Transactional
    public void deleteProduct(Integer pid) {
        Product product = PdRepo.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + pid));
        try {
            log.info("상품 삭제 시작: pid={}", pid);

            long cartCount = cartRepo.countByProduct_Pid(pid);
            if (cartCount > 0) {
                cartRepo.deleteByProduct_Pid(pid);
            }

            long reviewCount = mRvRepo.countByProduct_Pid(pid);
            if (reviewCount > 0) {
                mRvRepo.deleteByProduct_Pid(pid);
            }

            long wishlistCount = uPicRepo.countByProduct_Pid(pid);
            if (wishlistCount > 0) {
                uPicRepo.deleteByProduct_Pid(pid);
            }

            // 대표 이미지 삭제
            String imgFilename = product.getImage();
            if (imgFilename != null && !imgFilename.isEmpty()) {
                deleteImageFile(imgFilename);
            }

            // 상세 이미지 폴더 삭제
            deleteDetailDir(pid);

            PdRepo.deleteById(pid);
            log.info("상품 삭제 완료: pid={}", pid);

        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("상품 삭제 실패: 다른 데이터에서 참조되고 있어 삭제할 수 없습니다.");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("상품 삭제 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("상품 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /* ===== 대표 이미지 저장/토글 ===== */

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
            product.setHit(product.getHit());
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

    /* ============================ 통계/유틸 ============================ */

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
        if ((mainCategory == null || mainCategory.isBlank()) && (search == null || search.isBlank())) {
            return PdRepo.findAll(pageable);
        } else if (mainCategory != null && !mainCategory.isBlank() && (search == null || search.isBlank())) {
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
                .shippingCost(product.shippingCost())
                .build();
    }

    /* ============================ 파일 저장 공통 ============================ */

    /**
     * 대표 이미지(루트 img/에 저장)
     */
    public String saveImageFile(MultipartFile file) throws IOException {
        File uploadDir = new File(uploadDirectory);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        String originalFilename = Objects.requireNonNull(file.getOriginalFilename(), "파일명이 없습니다.");
        String ext = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String savedFilename = UUID.randomUUID() + ext;

        Path path = Paths.get(uploadDirectory, savedFilename);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        return savedFilename;
    }

    private void deleteImageFile(String filename) {
        try {
            Files.deleteIfExists(Paths.get(uploadDirectory, filename));
        } catch (IOException e) {
            log.error("이미지 파일 삭제 실패: {}", e.getMessage());
        }
    }

    /* ============================ 상세 이미지(폴더) ============================ */

    /**
     * 상세이미지 저장 경로: {uploadDirectory}/products/{pid}/
     */
    private Path detailDir(Integer pid) {
        return Paths.get(uploadDirectory, "products", String.valueOf(pid));
    }

    /**
     * 상세 이미지 저장 (폴더에 UUID명으로 저장) 후
     * 프론트에서 바로 사용할 경로("products/{pid}/{filename}") 목록을 반환.
     */
    public List<String> saveDetailImages(Integer pid, List<MultipartFile> files) {
        if (pid == null || files == null || files.isEmpty()) return List.of();

        try {
            Path dir = detailDir(pid);
            Files.createDirectories(dir);

            List<String> saved = new ArrayList<>();
            for (MultipartFile f : files) {
                if (f.isEmpty()) continue;
                String original = Objects.requireNonNullElse(f.getOriginalFilename(), "file");
                String ext = original.contains(".") ? original.substring(original.lastIndexOf(".")) : "";
                String name = UUID.randomUUID() + ext;
                Files.copy(f.getInputStream(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
                saved.add("products/" + pid + "/" + name); // /api/images/ 와 결합해서 사용
            }
            return saved;
        } catch (IOException e) {
            throw new RuntimeException("상세 이미지 저장 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 상세 이미지 목록 조회 (파일 시스템 탐색)
     */
    @Transactional(readOnly = true)
    public List<String> listDetailImages(Integer pid) {
        try {
            Path dir = detailDir(pid);
            if (!Files.exists(dir)) return List.of();
            try (Stream<Path> s = Files.list(dir)) {
                return s.filter(Files::isRegularFile)
                        .sorted()
                        .map(p -> "products/" + pid + "/" + p.getFileName())
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.warn("상세 이미지 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 상세 이미지 단건 삭제(선택)
     */
    public boolean deleteDetailImage(Integer pid, String filename) {
        try {
            Path p = detailDir(pid).resolve(filename);
            return Files.deleteIfExists(p);
        } catch (IOException e) {
            log.warn("상세 이미지 삭제 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 상세 이미지 폴더 통째 삭제(상품 삭제 시)
     */
    private void deleteDetailDir(Integer pid) {
        Path dir = detailDir(pid);
        if (!Files.exists(dir)) return;
        try (Stream<Path> s = Files.walk(dir)) {
            s.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    log.warn("상세 이미지 폴더 삭제 실패: {}", e.getMessage());
                }
            });
        } catch (IOException e) {
            log.warn("상세 이미지 폴더 순회 실패: {}", e.getMessage());
        }
    }
}
