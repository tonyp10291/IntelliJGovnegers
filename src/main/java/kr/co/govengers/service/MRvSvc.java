package kr.co.govengers.service;

import kr.co.govengers.entity.Review;
import kr.co.govengers.repository.MRvRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MRvSvc {

    private final MRvRepo mRvRepo;

    /**
     * 모든 리뷰 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<Review> getAllReviews(Pageable pageable) {
        return mRvRepo.findAll(pageable);
    }

    /**
     * 리뷰 검색 (상품명 기준)
     */
    @Transactional(readOnly = true)
    public Page<Review> searchReviews(String keyword, Pageable pageable) {
        return mRvRepo.findByProductPnmContainingIgnoreCase(keyword, pageable);
    }

    /**
     * 리뷰 ID로 조회
     */
    @Transactional(readOnly = true)
    public Review getReviewById(Long reviewId) {
        Optional<Review> review = mRvRepo.findById(reviewId);
        return review.orElse(null);
    }

    /**
     * 리뷰 삭제
     */
    public boolean deleteReview(Long reviewId) {
        if (mRvRepo.existsById(reviewId)) {
            mRvRepo.deleteById(reviewId);
            return true;
        }
        return false;
    }

    /**
     * 리뷰 답변 저장/수정
     */
    public Review saveResponse(Long reviewId, String responseContent) {
        Optional<Review> optionalReview = mRvRepo.findById(reviewId);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setResponse(responseContent);
            review.setResponseDate(LocalDateTime.now());
            return mRvRepo.save(review);
        }
        return null;
    }

    /**
     * 리뷰 답변 삭제
     */
    public Review deleteResponse(Long reviewId) {
        Optional<Review> optionalReview = mRvRepo.findById(reviewId);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setResponse(null);
            review.setResponseDate(null);
            return mRvRepo.save(review);
        }
        return null;
    }

    /**
     * 리뷰 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getReviewStats() {
        Map<String, Object> stats = new HashMap<>();

        // 전체 리뷰 수
        long totalReviews = mRvRepo.count();
        stats.put("totalReviews", totalReviews);

        // 답변 완료된 리뷰 수
        long answeredReviews = mRvRepo.countByResponseIsNotNull();
        stats.put("answeredReviews", answeredReviews);

        // 답변 미완료 리뷰 수
        long unansweredReviews = totalReviews - answeredReviews;
        stats.put("unansweredReviews", unansweredReviews);

        // 평균 별점
        Double averageRating = mRvRepo.findAverageRating();
        stats.put("averageRating", averageRating != null ? Math.round(averageRating * 10) / 10.0 : 0.0);

        // 별점별 리뷰 수
        for (int i = 1; i <= 5; i++) {
            long count = mRvRepo.countByRating(i);
            stats.put("rating" + i + "Count", count);
        }

        // 오늘 작성된 리뷰 수
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long todayReviews = mRvRepo.countByCreatedAtBetween(startOfDay, endOfDay);
        stats.put("todayReviews", todayReviews);

        return stats;
    }

    /**
     * 사용자별 리뷰 조회
     */
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByUser(String userId, Pageable pageable) {
        return mRvRepo.findByUserUid(userId, pageable);
    }

    /**
     * 상품별 리뷰 조회
     */
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByProduct(Long productId, Pageable pageable) {
        return mRvRepo.findByProductPid(productId, pageable);
    }

    /**
     * 별점별 리뷰 조회
     */
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByRating(int rating, Pageable pageable) {
        return mRvRepo.findByRating(rating, pageable);
    }

    /**
     * 답변 미완료 리뷰 조회
     */
    @Transactional(readOnly = true)
    public Page<Review> getUnansweredReviews(Pageable pageable) {
        return mRvRepo.findByResponseIsNull(pageable);
    }

    /**
     * 답변 완료 리뷰 조회
     */
    @Transactional(readOnly = true)
    public Page<Review> getAnsweredReviews(Pageable pageable) {
        return mRvRepo.findByResponseIsNotNull(pageable);
    }
}