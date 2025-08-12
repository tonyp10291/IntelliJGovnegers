package kr.co.govengers.repository;

import kr.co.govengers.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepo extends JpaRepository<Product, Integer> {
    // Integer ⇢ Product 엔티티의 @Id 타입으로 변경 (예: Long이면 Long)
}