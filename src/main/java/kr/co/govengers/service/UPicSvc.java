package kr.co.govengers.service;

import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.Wishlist;
import kr.co.govengers.repository.PdRepo;
import kr.co.govengers.repository.UPicRepo;
import kr.co.govengers.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UPicSvc {
    private final UPicRepo uPicRepo;
    private final PdRepo pdRepo;
    private final UserRepo userRepo;

    public Page<Wishlist> getGuestWishlist(String guestId, Pageable pageable){
        return uPicRepo.findByGuestIdWithProduct(guestId, pageable);
    }

    public boolean addWishlist(Integer productId, String guestId){
        Wishlist existWishlist = uPicRepo.findByIdAndGuestId(productId, guestId);

        if (existWishlist != null){
            return false;
        }

        Product pid = pdRepo.findById(productId).orElseThrow();
        Wishlist newWishlist = Wishlist.builder().product(pid).guestId(guestId).build();

        uPicRepo.save(newWishlist);

        return true;
    }
}