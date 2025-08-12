package kr.co.govengers.repository;

import kr.co.govengers.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface UPicRepo extends JpaRepository<Wishlist, Long> {

    long countByProduct_Pid(Integer pid);

    @Modifying
    @Transactional
    void deleteByProduct_Pid(Integer pid);

    @Query("SELECT w FROM Wishlist w JOIN FETCH w.product WHERE w.guestId = :guestId")
    Page<Wishlist> findByGuestIdWithProduct(String guestId, Pageable pageable);

    Optional<Wishlist> findByGuestIdAndProductPid(String guestId, Integer productPid);

    Optional<Wishlist> findByUserUidAndProductPid(String uid, Integer productPid);

    Optional<Wishlist> findByIdAndGuestId(Long id, String guestId);

    List<Wishlist> findByGuestId(String guestId);

    @Query("SELECT w FROM Wishlist w JOIN FETCH w.product WHERE w.user.uid = :uid")
    Page<Wishlist> findByUserUidWithProduct(String uid, Pageable pageable);

    Optional<Wishlist> findByIdAndUserUid(Long id, String uid);

    List<Wishlist> findByUserUid(String uid);

    void deleteByAddedAtBefore(LocalDateTime beforeDate);
}
