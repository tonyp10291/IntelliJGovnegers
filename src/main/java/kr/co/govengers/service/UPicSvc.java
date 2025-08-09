package kr.co.govengers.service;

import kr.co.govengers.dto.WishlistDTO;
import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.User;
import kr.co.govengers.entity.Wishlist;
import kr.co.govengers.repository.PdRepo;
import kr.co.govengers.repository.UPicRepo;
import kr.co.govengers.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UPicSvc {
    private final UPicRepo uPicRepo;
    private final PdRepo pdRepo;
    private final UserRepo userRepo;

    public Page<WishlistDTO> getGuestWishlist(String guestId, Pageable pageable){
        Page<Wishlist> wishlistPage = uPicRepo.findByGuestIdWithProduct(guestId, pageable);

        return wishlistPage.map(wishlist -> {
            Product product = wishlist.getProduct();
            WishlistDTO dto = new WishlistDTO();

            dto.setId(wishlist.getId());
            dto.setPid(product.getPid());
            dto.setImage(product.getImage());
            dto.setPnm(product.getPnm());
            dto.setPrice(product.getPrice());
            dto.setPoint(product.calculatePoint());
            dto.setShippingCost(product.shippingCost());
            dto.setTotalPrice(
                    product.getPrice() + dto.getShippingCost()
            );
            return dto;
        });
    }

    public boolean addWishlist(String guestId, Integer productPid){
        Optional<Wishlist> existWishlist = uPicRepo.findByGuestIdAndProductPid(guestId, productPid);

        if (existWishlist.isPresent()){
            return false;
        }

        Product pid = pdRepo.findById(productPid).orElseThrow();
        Wishlist newWishlist = Wishlist.builder().product(pid).guestId(guestId).build();

        uPicRepo.save(newWishlist);

        return true;
    }

    public boolean addUserWishlist(String uid, Integer productPid){
        Optional<Wishlist> existWishlist = uPicRepo.findByUserUidAndProductPid(uid, productPid);

        if (existWishlist.isPresent()){
            return false;
        }

        Product product = pdRepo.findById(productPid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품 ID: " + productPid));
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 ID: " + uid));

        Wishlist newWishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        uPicRepo.save(newWishlist);

        return true;
    }

    public boolean deleteWishlist(Long wishlistId, String guestId){
        Optional<Wishlist> wishlist = uPicRepo.findByIdAndGuestId(wishlistId, guestId);

        if (wishlist.isPresent()){
            uPicRepo.delete(wishlist.get());
            return true;
        }else{
            return false;
        }
    }

    public boolean deleteWishlistAll(String guestId){
        List<Wishlist> wishlists = uPicRepo.findByGuestId(guestId);

        if (!wishlists.isEmpty()){
            uPicRepo.deleteAll(wishlists);
            return true;
        }else{
            return false;
        }
    }

    public boolean migrateGuestWishlist(String guestId, User user) {
        List<Wishlist> guestWishlists = uPicRepo.findByGuestId(guestId);

        if (guestWishlists.isEmpty()) {
            return true;
        }

        List<Integer> existingUserProductPids = uPicRepo.findByUserUid(user.getUid())
                .stream()
                .map(w -> w.getProduct().getPid())
                .collect(Collectors.toList());
        List<Wishlist> newWishlists = new ArrayList<>();
        for (Wishlist guestWishlist : guestWishlists) {
            if (!existingUserProductPids.contains(guestWishlist.getProduct().getPid())) {
                Wishlist newWishlist = Wishlist.builder()
                        .user(user)
                        .product(guestWishlist.getProduct())
                        .addedAt(LocalDateTime.now())
                        .build();
                newWishlists.add(newWishlist);
            }
        }
        uPicRepo.saveAll(newWishlists);
        uPicRepo.deleteAll(guestWishlists);

        return true;
    }

    public Page<WishlistDTO> getUserWishlist(String uid, Pageable pageable){
        Page<Wishlist> wishlistPage = uPicRepo.findByUserUidWithProduct(uid, pageable);

        return wishlistPage.map(wishlist -> {
            Product product = wishlist.getProduct();
            WishlistDTO dto = new WishlistDTO();
            dto.setId(wishlist.getId());
            dto.setPid(product.getPid());
            dto.setImage(product.getImage());
            dto.setPnm(product.getPnm());
            dto.setPrice(product.getPrice());
            dto.setPoint(product.calculatePoint());
            dto.setShippingCost(product.shippingCost());
            dto.setTotalPrice(
                    product.getPrice() + dto.getShippingCost()
            );
            return dto;
        });
    }

    public boolean deleteUserWishlist(Long wishlistId, String uid){
        Optional<Wishlist> wishlist = uPicRepo.findByIdAndUserUid(wishlistId, uid);
        if (wishlist.isPresent()){
            uPicRepo.delete(wishlist.get());
            return true;
        }
        return false;
    }

    public boolean deleteUserWishlistAll(String uid){
        List<Wishlist> wishlists = uPicRepo.findByUserUid(uid);
        if (!wishlists.isEmpty()){
            uPicRepo.deleteAll(wishlists);
            return true;
        }
        return false;
    }

    @Scheduled(cron = "0 5 0 * * ?")
    public void deleteExpiredWishlists() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        uPicRepo.deleteByAddedAtBefore(sevenDaysAgo);
        System.out.println("7일이 지난 위시리스트 항목이 삭제되었습니다.");
    }
}
