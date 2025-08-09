package kr.co.govengers.controller;

import kr.co.govengers.entity.Review;
import kr.co.govengers.service.MRvSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class MRvController {

    private final MRvSvc mRvSvc;

    @GetMapping("/list")
    public ResponseEntity<?> getReviewList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("reviewId").descending());

            Page<Review> reviewPage;
            if (keyword != null && !keyword.trim().isEmpty()) {
                reviewPage = mRvSvc.searchReviews(keyword, pageable);
            } else {
                reviewPage = mRvSvc.getAllReviews(pageable);
            }

            return ResponseEntity.ok(reviewPage);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("리뷰 목록 조회 실패: " + e.getMessage());
        }
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReview(@PathVariable Long reviewId) {

        try {
            Review review = mRvSvc.getReviewById(reviewId);
            if (review == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(review);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("리뷰 조회 실패: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {

        try {
            boolean deleted = mRvSvc.deleteReview(reviewId);
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("리뷰 삭제 실패: " + e.getMessage());
        }
    }

    @PostMapping("/response/{reviewId}")
    public ResponseEntity<?> saveResponse(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> request) {

        try {
            String responseContent = request.get("response");

            Review updatedReview = mRvSvc.saveResponse(reviewId, responseContent);
            if (updatedReview == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(updatedReview);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("답변 저장 실패: " + e.getMessage());
        }
    }

    @DeleteMapping("/response/{reviewId}")
    public ResponseEntity<?> deleteResponse(@PathVariable Long reviewId) {

        try {
            Review updatedReview = mRvSvc.deleteResponse(reviewId);
            if (updatedReview == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(updatedReview);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("답변 삭제 실패: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getReviewStats() {

        try {
            Map<String, Object> stats = mRvSvc.getReviewStats();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("통계 조회 실패: " + e.getMessage());
        }
    }
}