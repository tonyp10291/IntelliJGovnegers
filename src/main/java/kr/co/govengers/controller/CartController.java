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

    // 장바구니 목록 조회 (비로그인, 로그인 모두 처리)
    @GetMapping
    public ResponseEntity<Page<CartItemDTO>> getCart(
            @AuthenticationPrincipal User user, // <-- 수정: @AuthenticationPrincipal 사용
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String guestId) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<CartItemDTO> cartItems;

        // user 객체가 null이 아니면 로그인 사용자
        if (user != null) {
            // guestId가 있으면 마이그레이션 로직 실행
            if (guestId != null && !guestId.isEmpty()) {
                cartSvc.migrateCart(guestId, user.getUid());
            }
            cartItems = cartSvc.getUserCart(user.getUid(), pageable);
        } else if (guestId != null && !guestId.isEmpty()) {
            // 비로그인 사용자 로직
            cartItems = cartSvc.getGuestCart(guestId, pageable);
        } else {
            // 사용자 정보가 없는 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(cartItems);
    }

    // 장바구니 상품 추가 (비로그인, 로그인 모두 처리)
    @PostMapping("/add")
    public ResponseEntity<String> addCartItem(
            @RequestParam Integer pid,
            @RequestParam(defaultValue = "1") Integer quantity,
            @AuthenticationPrincipal User user, // <-- 수정: @AuthenticationPrincipal 사용
            @RequestParam(required = false) String guestId) {

        try {
            if (user != null) {
                cartSvc.addCartItem(user.getUid(), pid, quantity);
            } else if (guestId != null && !guestId.isEmpty()) {
                cartSvc.addGuestCartItem(guestId, pid, quantity);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용자 정보가 없습니다.");
            }
            return ResponseEntity.ok("장바구니에 상품이 추가되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("장바구니 추가 실패");
        }
    }

    // 장바구니 전체 비우기 (비로그인, 로그인 모두 처리)
    @PostMapping("/delete/all")
    public ResponseEntity<String> clearCart(
            @AuthenticationPrincipal User user, // <-- 수정: @AuthenticationPrincipal 사용
            @RequestParam(required = false) String guestId) {
        try {
            if (user != null) {
                cartSvc.clearUserCart(user.getUid());
            } else if (guestId != null && !guestId.isEmpty()) {
                cartSvc.clearGuestCart(guestId);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용자 정보가 없습니다.");
            }
            return ResponseEntity.ok("장바구니를 비웠습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("장바구니 비우기 실패");
        }
    }

    // 장바구니 상품 수량 변경
    @PostMapping("/update-quantity")
    public ResponseEntity<String> updateCartItemQuantity(
            @RequestParam Integer cartId,
            @RequestParam Integer quantity) {
        try {
            cartSvc.updateCartItemQuantity(cartId, quantity);
            return ResponseEntity.ok("장바구니 상품 수량이 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // 장바구니 개별 상품 삭제 (비로그인, 로그인 모두 처리)
    @DeleteMapping("/delete/{cartId}")
    public ResponseEntity<String> deleteCartItem(
            @PathVariable Integer cartId,
            @AuthenticationPrincipal User user, // <-- 수정: @AuthenticationPrincipal 사용
            @RequestParam(required = false) String guestId) {
        try {
            if (user != null) {
                cartSvc.deleteCartItem(cartId, user.getUid()); // <-- 서비스 메서드 파라미터 수정 필요
            } else if (guestId != null && !guestId.isEmpty()) {
                cartSvc.deleteGuestCartItem(guestId, cartId);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용자 정보가 없습니다.");
            }
            return ResponseEntity.ok("장바구니 상품이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("장바구니 상품 삭제 실패");
        }
    }

    // 선택한 상품들 삭제 (비로그인, 로그인 모두 처리)
    @PostMapping("/delete/checked")
    public ResponseEntity<String> deleteCheckedCartItems(
            @RequestBody List<Integer> cartIds,
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String guestId) {
        try {
            if (user != null) {
                cartSvc.deleteCartItems(cartIds, user.getUid());
            } else if (guestId != null && !guestId.isEmpty()) {
                cartSvc.deleteGuestCartItems(guestId, cartIds);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용자 정보가 없습니다.");
            }
            return ResponseEntity.ok("선택한 상품들이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("선택한 상품 삭제 실패");
        }
    }
}