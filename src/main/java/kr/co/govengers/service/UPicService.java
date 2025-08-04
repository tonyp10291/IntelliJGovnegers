package kr.co.govengers.service;

import kr.co.govengers.entity.Wishlist;
import kr.co.govengers.repository.UPicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UPicService {
    private final UPicRepository uPicRepository;

    public Page<Wishlist> getGuestWishlist(String guestId, Pageable pageable){
        return uPicRepository.findByGuestIdWithProduct(guestId, pageable);
    }

    //repository.save(wishlist)
}
