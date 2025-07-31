package kr.co.govengers.repository;

import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.enums.MainCategory;
import kr.co.govengers.entity.enums.SubCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepo extends JpaRepository<Product, Integer> {

    // 메인 카테고리로 상품 목록을 찾아오는 기능 (페이지네이션 포함)
    Page<Product> findByMainCategory(MainCategory mainCategory, Pageable pageable);

    // 서브 카테고리로 상품 목록을 찾아오는 기능 (페이지네이션 포함)
    Page<Product> findBySubCategory(SubCategory subCategory, Pageable pageable);

    // 상품 이름(pnm)에 특정 키워드가 포함된 상품을 검색하는 기능 (페이지네이션 포함)
    Page<Product> findByPnmContainingIgnoreCase(String keyword, Pageable pageable);
}