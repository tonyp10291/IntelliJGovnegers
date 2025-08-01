package kr.co.govengers.repository;

import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.enums.MainCategory;
import kr.co.govengers.entity.enums.SubCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PdRepo extends JpaRepository<Product, Integer> {

    Page<Product> findByMainCategory(MainCategory mainCategory, Pageable pageable);
    Page<Product> findBySubCategory(SubCategory subCategory, Pageable pageable);
    Page<Product> findByPnmContainingIgnoreCase(String keyword, Pageable pageable);
}