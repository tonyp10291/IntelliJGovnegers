package kr.co.govengers.repository;

import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.enums.MainCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PdRepo extends JpaRepository<Product, Integer> {

    List<Product> findByMainCategory(MainCategory mainCategory);
    List<Product> findByPnmContainingIgnoreCase(String keyword);
    List<Product> findByPriceBetween(int minPrice, int maxPrice);
    List<Product> findByOrigin(String origin);
    List<Product> findBySoldout(int soldout);

    @Query("SELECT p FROM Product p ORDER BY p.hit DESC")
    List<Product> findPopularProducts();

    @Query("SELECT p FROM Product p ORDER BY p.pid DESC")
    List<Product> findLatestProducts();

    @Query("SELECT p FROM Product p WHERE " +
            "(:keyword IS NULL OR LOWER(p.pnm) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:category IS NULL OR p.mainCategory = :category)")
    List<Product> findByKeywordAndCategory(@Param("keyword") String keyword,
                                           @Param("category") MainCategory category);
    Page<Product> findByMainCategory(MainCategory mainCategory, Pageable pageable);
    Page<Product> findByPnmContainingIgnoreCase(String keyword, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.hit = p.hit + 1 WHERE p.pid = :pid")
    void increaseHit(@Param("pid") Integer pid);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.price = :price WHERE p.pid = :pid")
    void updatePrice(@Param("pid") Integer pid, @Param("price") int price);

    @Query("SELECT p FROM Product p WHERE p.expDate <= :date AND p.expDate IS NOT NULL")
    List<Product> findProductsWithExpiringDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.soldout = 0")
    long countAvailableProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.mainCategory = :category AND p.soldout = 0")
    long countByCategoryAndAvailable(@Param("category") MainCategory category);

    List<Product> findAllByOrderByHitDesc();
    List<Product> findByPnmContainingIgnoreCaseAndMainCategory(String keyword, MainCategory category);
    List<Product> findAllByOrderByPidDesc();




}