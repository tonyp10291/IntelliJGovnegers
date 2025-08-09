package kr.co.govengers.controller;

import kr.co.govengers.dto.CartItemDTO;
import kr.co.govengers.entity.User;
import kr.co.govengers.service.CartSvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartSvc cartSvc;

    public CartController(CartSvc cartSvc) {
        this.cartSvc = cartSvc;
    }

    // 로그인 사용자 장바구니 목록 조회
    @GetMapping("/user")
    public ResponseEntity<Page<CartItemDTO>> getUserCart(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Pageable pageable = PageRequest.of(page, 5);
        Page<CartItemDTO> cartItems = cartSvc.getUserCart(user.getUid(), pageable);
        return ResponseEntity.ok(cartItems);
    }

    // 비회원 장바구니 목록 조회
    @GetMapping("/guest")
    public ResponseEntity<Page<CartItemDTO>> getGuestCart(
            @RequestParam String guestId,
            @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 5);
        Page<CartItemDTO> cartItems = cartSvc.getGuestCart(guestId, pageable);
        return ResponseEntity.ok(cartItems);
    }

    // 로그인 후 장바구니 마이그레이션
    @PostMapping("/migrate")
    public ResponseEntity<String> migrateCart(
            @RequestParam String guestId,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        cartSvc.migrateCart(guestId, user.getUid());
        return ResponseEntity.ok("장바구니가 성공적으로 마이그레이션되었습니다.");
    }

    // 장바구니 상품 추가 (로그인)
    @PostMapping("/user/add")
    public ResponseEntity<String> addCartItem(@AuthenticationPrincipal User user, @RequestParam Integer pid, @RequestParam(defaultValue = "1") Integer quantity) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            cartSvc.addCartItem(user.getUid(), pid, quantity);
            return ResponseEntity.ok("장바구니에 상품이 추가되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 장바구니 상품 추가 (비회원)
    @PostMapping("/guest/add")
    public ResponseEntity<String> addGuestCartItem(@RequestParam String guestId, @RequestParam Integer pid, @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            cartSvc.addGuestCartItem(guestId, pid, quantity);
            return ResponseEntity.ok("장바구니에 상품이 추가되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 장바구니 상품 수량 변경 (로그인, 비회원 공용)
    @PostMapping("/update-quantity")
    public ResponseEntity<String> updateCartItemQuantity(@RequestParam Integer cartId, @RequestParam Integer quantity) {
        try {
            cartSvc.updateCartItemQuantity(cartId, quantity);
            return ResponseEntity.ok("장바구니 상품 수량이 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // 장바구니 개별 상품 삭제 (로그인)
    @DeleteMapping("/user/delete/{cartId}")
    public ResponseEntity<String> deleteUserCartItem(@PathVariable Integer cartId, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            cartSvc.deleteCartItem(cartId, user.getUid());
            return ResponseEntity.ok("장바구니 상품이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // 장바구니 개별 상품 삭제 (비회원)
    @DeleteMapping("/guest/delete/{cartId}")
    public ResponseEntity<String> deleteGuestCartItem(@PathVariable Integer cartId, @RequestParam String guestId) {
        try {
            cartSvc.deleteGuestCartItem(guestId, cartId);
            return ResponseEntity.ok("장바구니 상품이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // 선택한 상품들 삭제 (로그인)
    @PostMapping("/user/delete-checked")
    public ResponseEntity<String> deleteUserCheckedCartItems(@RequestBody List<Integer> cartIds, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            cartSvc.deleteCartItems(cartIds, user.getUid());
            return ResponseEntity.ok("선택한 상품들이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("선택한 상품 삭제 실패");
        }
    }

    // 선택한 상품들 삭제 (비회원)
    @PostMapping("/guest/delete-checked")
    public ResponseEntity<String> deleteGuestCheckedCartItems(@RequestBody List<Integer> cartIds, @RequestParam String guestId) {
        try {
            cartSvc.deleteGuestCartItems(guestId, cartIds);
            return ResponseEntity.ok("선택한 상품들이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("선택한 상품 삭제 실패");
        }
    }

    // 장바구니 전체 비우기 (로그인)
    @PostMapping("/user/clear")
    public ResponseEntity<String> clearUserCart(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            cartSvc.clearUserCart(user.getUid());
            return ResponseEntity.ok("장바구니를 비웠습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("장바구니 비우기 실패");
        }
    }

    // 장바구니 전체 비우기 (비회원)
    @PostMapping("/guest/clear")
    public ResponseEntity<String> clearGuestCart(@RequestParam String guestId) {
        try {
            cartSvc.clearGuestCart(guestId);
            return ResponseEntity.ok("장바구니를 비웠습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("장바구니 비우기 실패");
        }
    }
}