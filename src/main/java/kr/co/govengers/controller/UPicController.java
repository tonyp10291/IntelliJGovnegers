package kr.co.govengers.controller;//package kr.co.govengers.controller;
//
//import kr.co.govengers.entity.Inquiry;
//import kr.co.govengers.entity.Product;
//import kr.co.govengers.repository.ProductRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/wishlist")
//@RequiredArgsConstructor
//public class UPicController {
//
//    private final ProductRepository productRepository;
//
//    @PostMapping
//    public ResponseEntity<List<Product>> getProducts(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(required = false) String keyword,
//            @RequestParam(required = false) String category,
//            @RequestParam(required = false) String answerStatus,
//            @RequestParam(required = false) Boolean isPrivate
//    ) {
//            Pageable pageable = PageRequest.of(page, 10);
//
//            if (keyword != null && !keyword.isBlank()) {
//                return mqnaSvc.searchInquiriesByKeyword(keyword, pageable);
//            }
//
//            if (category != null && !category.isBlank()) {
//                return mqnaSvc.getInquiriesByCategory(category, pageable);
//            }
//
//            if (answerStatus != null && !answerStatus.isBlank()) {
//                return mqnaSvc.getInquiriesByAnswerStatus(answerStatus, pageable);
//            }
//
//            if (isPrivate != null) {
//                return mqnaSvc.getInquiriesByPrivacy(isPrivate, pageable);
//            }
//
//            return mqnaSvc.getPagedInquiries(pageable);
//        }
//    }
//
//    @PostMapping("/user")
//}
