package kr.co.govengers.repository;

import kr.co.govengers.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepo extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProduct_PidOrderByIdAsc(Integer pid);
}