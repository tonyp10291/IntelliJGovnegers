package kr.co.govengers.controller;

import kr.co.govengers.entity.Product;
import kr.co.govengers.entity.Wishlist;
import kr.co.govengers.service.UPicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class UPicController {

    private final UPicService uPicService;

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

        Page<Wishlist> wishlist = uPicService.getGuestWishlist(guestId, pageable);
        return ResponseEntity.ok(wishlist);

//        if (keyword != null && !keyword.isBlank()) {
//            return mqnaSvc.searchInquiriesByKeyword(keyword, pageable);
//        }
//
//        if (category != null && !category.isBlank()) {
//            return mqnaSvc.getInquiriesByCategory(category, pageable);
//        }
//
//        if (answerStatus != null && !answerStatus.isBlank()) {
//            return mqnaSvc.getInquiriesByAnswerStatus(answerStatus, pageable);
//        }
//
//        if (isPrivate != null) {
//            return mqnaSvc.getInquiriesByPrivacy(isPrivate, pageable);
//        }
//
//        return mqnaSvc.getPagedInquiries(pageable);
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
