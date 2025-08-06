package kr.co.govengers.repository;

import kr.co.govengers.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface UPicRepo extends JpaRepository<Wishlist, Long> {

    @Query("SELECT w FROM Wishlist w JOIN FETCH w.product WHERE w.guestId = :guestId")
    Page<Wishlist> findByGuestIdWithProduct(String guestId, Pageable pageable);

    Wishlist findByIdAndGuestId(Integer pid, String guestId);
}