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

    long countByProduct_Pid(Integer pid);

    @Modifying
    @Transactional
    void deleteByProduct_Pid(Integer pid);

    @Query("SELECT c FROM Cart c JOIN FETCH c.product WHERE c.user = :user")
    Page<Cart> findByUserWithProduct(@Param("user") User user, Pageable pageable);

    @Query("SELECT c FROM Cart c JOIN FETCH c.product WHERE c.guestId = :guestId")
    Page<Cart> findByGuestIdWithProduct(@Param("guestId") String guestId, Pageable pageable);

    List<Cart> findByGuestId(String guestId);

    List<Cart> findByUser(User user);

    Optional<Cart> findByUserAndProduct_Pid(User user, Integer pid);

    Optional<Cart> findByGuestIdAndProduct_Pid(String guestId, Integer pid);

    @Modifying
    @Transactional
    void deleteAllByUser(User user);

    @Modifying
    @Transactional
    void deleteAllByGuestId(String guestId);

    @Modifying
    @Transactional
    void deleteByCartId(Integer cartId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Cart c WHERE c.cartId IN :cartIds")
    void deleteAllByCartIdIn(@Param("cartIds") List<Integer> cartIds);
}