package kr.co.govengers.service;

import kr.co.govengers.dto.CartItemDTO;
import kr.co.govengers.entity.Cart;
import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.User;
import kr.co.govengers.repository.CartRepo;
import kr.co.govengers.repository.PdRepo;
import kr.co.govengers.repository.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CartSvc {

    private final CartRepo cartRepo;
    private final UserRepo userRepo;
    private final PdRepo pdRepo;

    public CartSvc(CartRepo cartRepo, UserRepo userRepo, PdRepo pdRepo) {
        this.cartRepo = cartRepo;
        this.userRepo = userRepo;
        this.pdRepo = pdRepo;
    }

    public Page<CartItemDTO> getUserCart(String uid, Pageable pageable) {
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return cartRepo.findByUserWithProduct(user, pageable).map(CartItemDTO::from);
    }

    public Page<CartItemDTO> getGuestCart(String guestId, Pageable pageable) {
        return cartRepo.findByGuestIdWithProduct(guestId, pageable).map(CartItemDTO::from);
    }

    @Transactional
    public void addCartItem(String uid, Integer pid, Integer quantity) {
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Product product = pdRepo.findByPid(pid)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Optional<Cart> existingCartItem = cartRepo.findByUserAndProduct_Pid(user, pid);

        if (existingCartItem.isPresent()) {
            Cart cart = existingCartItem.get();
            cart.setQuantity(cart.getQuantity() + quantity);
        } else {
            Cart newCartItem = Cart.builder()
                    .user(user)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cartRepo.save(newCartItem);
        }
    }

    @Transactional
    public void addGuestCartItem(String guestId, Integer pid, Integer quantity) {
        Product product = pdRepo.findByPid(pid)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Optional<Cart> existingCartItem = cartRepo.findByGuestIdAndProduct_Pid(guestId, pid);

        if (existingCartItem.isPresent()) {
            Cart cart = existingCartItem.get();
            cart.setQuantity(cart.getQuantity() + quantity);
        } else {
            Cart newCartItem = Cart.builder()
                    .guestId(guestId)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cartRepo.save(newCartItem);
        }
    }

    @Transactional
    public void updateCartItemQuantity(Integer cartId, Integer quantity) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        cart.setQuantity(quantity);
    }

    @Transactional
    public void deleteCartItem(Integer cartId, String uid) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!cart.getUser().getUid().equals(uid)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        cartRepo.deleteById(cartId);
    }

    @Transactional
    public void deleteGuestCartItem(String guestId, Integer cartId) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        if (!guestId.equals(cart.getGuestId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        cartRepo.deleteById(cartId);
    }

    @Transactional
    public void deleteCartItems(List<Integer> cartIds, String uid) {
        List<Cart> cartsToDelete = cartRepo.findAllById(cartIds);

        boolean allBelongToUser = cartsToDelete.stream()
                .allMatch(cart -> uid.equals(cart.getUser().getUid()));

        if (!allBelongToUser) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        cartRepo.deleteAllById(cartIds);
    }

    @Transactional
    public void deleteGuestCartItems(String guestId, List<Integer> cartIds) {
        List<Cart> cartsToDelete = cartRepo.findAllById(cartIds);
        boolean allBelongToGuest = cartsToDelete.stream()
                .allMatch(cart -> guestId.equals(cart.getGuestId()));

        if (!allBelongToGuest) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        cartRepo.deleteAllById(cartIds);
    }

    @Transactional
    public void clearUserCart(String uid) {
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        cartRepo.deleteAllByUser(user);
    }

    @Transactional
    public void clearGuestCart(String guestId) {
        cartRepo.deleteAllByGuestId(guestId);
    }

    @Transactional
    public boolean migrateCart(String guestId, String uid) {
        List<Cart> guestCarts = cartRepo.findByGuestId(guestId);

        if (guestCarts.isEmpty()) {
            return true;
        }

        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Integer> existingUserProductPids = cartRepo.findByUser(user)
                .stream()
                .map(c -> c.getProduct().getPid())
                .collect(Collectors.toList());

        List<Cart> cartsToMigrate = new ArrayList<>();
        for (Cart guestCart : guestCarts) {
            if (!existingUserProductPids.contains(guestCart.getProduct().getPid())) {
                Cart newCart = Cart.builder()
                        .user(user)
                        .product(guestCart.getProduct())
                        .quantity(guestCart.getQuantity())
                        .build();
                cartsToMigrate.add(newCart);
            }
        }
        cartRepo.saveAll(cartsToMigrate);
        cartRepo.deleteAll(guestCarts);

        return true;
    }

    @Scheduled(cron = "0 5 0 * * ?") //매일 새벽 0시 5분에 메서드 실행/비활성화가 정상임
    public void deleteExpiredCart() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        cartRepo.deleteByAddedAtBefore(sevenDaysAgo);
        System.out.println("7일이 지난 장바구니 항목이 삭제되었습니다.");
    }
}