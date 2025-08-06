package kr.co.govengers.controller;

import kr.co.govengers.dto.WishlistDTO;
import kr.co.govengers.entity.User;
import kr.co.govengers.entity.Wishlist;
import kr.co.govengers.service.UPicSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class UPicController {

    private final UPicSvc uPicSvc;

    @GetMapping("/guest")
    public ResponseEntity<Page<WishlistDTO>> getGuestWishlist(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam String guestId
    ) {
        //guestId 없을시
        if (guestId == null || guestId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Pageable pageable = PageRequest.of(page, 5);

        Page<WishlistDTO> wishlist = uPicSvc.getGuestWishlist(guestId, pageable);
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping("/guest/add")
    public ResponseEntity<String> setGuestWishlist(@RequestParam String guestId, @RequestParam Integer pid) {

        if (guestId == null || guestId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        boolean success = uPicSvc.addWishlist(guestId, pid);
        if (success) {
            return new ResponseEntity<>("wishlist 추가 성공", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("wishlist 추가 실패", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/user/add")
    public ResponseEntity<String> setUserWishlist(@RequestParam Integer pid, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean success = uPicSvc.addUserWishlist(user.getUid(), pid);
        if (success) {
            return new ResponseEntity<>("로그인 위시리스트 추가 성공", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("로그인 위시리스트 추가 실패", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/guest/delete")
    public ResponseEntity<String> deleteGuestWishlist(@RequestParam String guestId, @RequestParam Long id) {

        if (guestId == null || guestId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        boolean success = uPicSvc.deleteWishlist(id, guestId);
        if (success) {
            return new ResponseEntity<>("wishlist 삭제 성공", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("wishlist 삭제 실패", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/guest/delete/all")
    public ResponseEntity<String> deleteGuestWishlistAll(@RequestParam String guestId) {
        if (guestId == null || guestId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        boolean success = uPicSvc.deleteWishlistAll(guestId);
        if (success) {
            return new ResponseEntity<>("wishlist 삭제 성공", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("wishlist 삭제 실패", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/migrate")
    public ResponseEntity<String> migrateWishlist(@RequestParam String guestId, @AuthenticationPrincipal User user) {
        if (guestId == null || guestId.isEmpty() || user == null) {
            return ResponseEntity.badRequest().build();
        }
        boolean success = uPicSvc.migrateGuestWishlist(guestId, user);
        if (success) {
            return new ResponseEntity<>("Wishlist 비로그인 데이터 삭제 성공", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Wishlist 비로그인 데이터 삭제 실패", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<Page<WishlistDTO>> getUserWishlist(
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Pageable pageable = PageRequest.of(page, 5);
        Page<WishlistDTO> wishlist = uPicSvc.getUserWishlist(user.getUid(), pageable);
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping("/user/delete")
    public ResponseEntity<String> deleteUserWishlist(@RequestParam Long id, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean success = uPicSvc.deleteUserWishlist(id, user.getUid());
        if (success) {
            return new ResponseEntity<>("Wishlist 삭제 성공", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Wishlist 삭제 실패", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/user/delete/all")
    public ResponseEntity<String> deleteUserWishlistAll(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean success = uPicSvc.deleteUserWishlistAll(user.getUid());
        if (success) {
            return new ResponseEntity<>("Wishlist 삭제 성공", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Wishlist 삭제 실패", HttpStatus.BAD_REQUEST);
        }

    }
}
