package kr.co.govengers.repository;

import kr.co.govengers.entity.Cart;
import kr.co.govengers.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart, Integer> {
    // 회원 장바구니 목록 조회 (페이징) - JOIN FETCH 추가
    @Query("SELECT c FROM Cart c JOIN FETCH c.product WHERE c.user = :user")
    Page<Cart> findByUserWithProduct(@Param("user") User user, Pageable pageable);

    // 비회원 장바구니 목록 조회 (페이징) - JOIN FETCH 추가
    @Query("SELECT c FROM Cart c JOIN FETCH c.product WHERE c.guestId = :guestId")
    Page<Cart> findByGuestIdWithProduct(@Param("guestId") String guestId, Pageable pageable);

    // 마이그레이션을 위한 비회원 장바구니 전체 목록 조회
    List<Cart> findByGuestId(String guestId);

    // 마이그레이션을 위한 회원 장바구니 전체 목록 조회
    List<Cart> findByUser(User user);

    // 회원 장바구니 상품 존재 여부 확인
    Optional<Cart> findByUserAndProduct_Pid(User user, Integer pid);

    // 비회원 장바구니 상품 존재 여부 확인
    Optional<Cart> findByGuestIdAndProduct_Pid(String guestId, Integer pid);

    // 회원 장바구니 전체 삭제
    @Modifying
    @Transactional
    void deleteAllByUser(User user);

    // 비회원 장바구니 전체 삭제
    @Modifying
    @Transactional
    void deleteAllByGuestId(String guestId);

    // 특정 장바구니 항목 삭제 (회원/비회원 공용)
    @Modifying
    @Transactional
    void deleteByCartId(Integer cartId);

    // 특정 장바구니 항목 목록 삭제 (회원/비회원 공용)
    @Modifying
    @Transactional
    @Query("DELETE FROM Cart c WHERE c.cartId IN :cartIds")
    void deleteAllByCartIdIn(@Param("cartIds") List<Integer> cartIds);
}