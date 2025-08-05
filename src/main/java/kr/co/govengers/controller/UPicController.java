package kr.co.govengers.controller;

import kr.co.govengers.entity.Wishlist;
import kr.co.govengers.service.UPicSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class UPicController {

    private final UPicSvc uPicSvc;

    @GetMapping("/guest")
    public ResponseEntity<Page<Wishlist>> getGuestWishlist(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam String guestId
        ) {
        //guestId 없을시
        if (guestId == null || guestId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Pageable pageable = PageRequest.of(page, 5);

        Page<Wishlist> wishlist = uPicSvc.getGuestWishlist(guestId, pageable);
        System.out.println("wishlist??????????" + wishlist);
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping("/guest")
    public ResponseEntity<String> setGuestWishlist(@RequestParam String guestId, @RequestParam Integer pid){
        //guestId 없을시
        if (guestId == null || guestId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        boolean success = uPicSvc.addWishlist(pid, guestId);
        if (success){
            return new ResponseEntity<>("wishlist 추가 성공", HttpStatus.OK);
        }else{
            return new ResponseEntity<>("wishlist 추가 실패", HttpStatus.BAD_REQUEST);
        }
    }

//    @GetMapping("/user")
//    public ResponseEntity<List<Product>> getUserWishlist(
//        @RequestParam(defaultValue = "0") int page,
//        @RequestBody Map<String, String> payload,
//        @AuthenticationPrincipal User user
//    ){
//
//    }
}
